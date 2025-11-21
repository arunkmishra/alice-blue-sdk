package com.aliceblue.websocket

import zio._
import zio.stream._
import zio.json._
import sttp.client3._
import sttp.client3.ziojson._
import sttp.ws.{WebSocket, WebSocketFrame}
import sttp.capabilities.WebSockets
import sttp.capabilities.zio.ZioStreams
import com.aliceblue.models._
import java.security.MessageDigest
import java.nio.charset.StandardCharsets

object MarketData:
  val BaseUrl      = "https://ant.aliceblueonline.com/rest/AliceBlueAPIService/api"
  val WebSocketUrl = "wss://ws1.aliceblueonline.com/NorenWS/"

  // --- Helper Models for WebSocket ---
  case class SocketSessionRequest(loginType: String = "API")
  object SocketSessionRequest:
    given JsonEncoder[SocketSessionRequest] = DeriveJsonEncoder.gen

  case class SocketSessionResponse(stat: String, emsg: Option[String])
  object SocketSessionResponse:
    given JsonDecoder[SocketSessionResponse] = DeriveJsonDecoder.gen

  case class InitConnection(susertoken: String, t: String, actid: String, uid: String, source: String)
  object InitConnection:
    given JsonEncoder[InitConnection] = DeriveJsonEncoder.gen

  case class SubscriptionRequest(k: String, t: String)
  object SubscriptionRequest:
    given JsonEncoder[SubscriptionRequest] = DeriveJsonEncoder.gen

  // --- Implementation ---

  def connect(
      userId: String,
      sessionId: String,
      backend: SttpBackend[Task, ZioStreams & WebSockets]
  ): Task[MarketDataClient] =
    for
      _        <- prepareSocketSession(userId, sessionId, backend)
      encToken <- ZIO.attempt(generateEncToken(sessionId))
      queue    <- Queue.unbounded[String] // Queue for outgoing messages (subscriptions)
    // We return a client that can be used to subscribe/unsubscribe
    // The actual connection is managed in a background fiber or stream
    yield new MarketDataClient(userId, encToken, queue, backend)

  private def prepareSocketSession(userId: String, sessionId: String, backend: SttpBackend[Task, Any]): Task[Unit] =
    val invalidateReq = basicRequest
      .post(uri"$BaseUrl/ws/invalidateSocketSess")
      .header("Authorization", s"Bearer $userId $sessionId")
      .body(SocketSessionRequest())
      .response(asJson[SocketSessionResponse])

    val createReq = basicRequest
      .post(uri"$BaseUrl/ws/createSocketSess")
      .header("Authorization", s"Bearer $userId $sessionId")
      .body(SocketSessionRequest())
      .response(asJson[SocketSessionResponse])

    for
      _          <- backend.send(invalidateReq) // Ignore result as per pya3? Or check stat? pya3 checks stat.
      createResp <- backend.send(createReq)
      _ <- createResp.body match
        case Right(resp) if resp.stat == "Ok" => ZIO.unit
        case Right(resp) => ZIO.fail(new Exception(s"Failed to create socket session: ${resp.emsg}"))
        case Left(err)   => ZIO.fail(new Exception(s"Failed to create socket session request: $err"))
    yield ()

  private def generateEncToken(sessionId: String): String =
    val sha256_1 = sha256(sessionId)
    sha256(sha256_1)

  private def sha256(input: String): String =
    val digest  = MessageDigest.getInstance("SHA-256")
    val encoded = digest.digest(input.getBytes(StandardCharsets.UTF_8))
    encoded.map("%02x".format(_)).mkString

class MarketDataClient(
    userId: String,
    encToken: String,
    outgoingQueue: Queue[String],
    backend: SttpBackend[Task, ZioStreams & WebSockets]
):
  import MarketData._

  def subscribe(instruments: List[Instrument]): Task[Unit] =
    val scripts = instruments.map(i => s"${i.exchange}|${i.token}").mkString("#")
    val req     = SubscriptionRequest(k = scripts, t = "t") // t=t for token, t=d for depth
    outgoingQueue.offer(req.toJson).unit

  def unsubscribe(instruments: List[Instrument]): Task[Unit] =
    val scripts = instruments.map(i => s"${i.exchange}|${i.token}").mkString("#")
    val req     = SubscriptionRequest(k = scripts, t = "u")
    outgoingQueue.offer(req.toJson).unit

  // Stream of raw JSON messages for now. Can be parsed into MarketDataUpdate objects.
  val stream: ZStream[Any, Throwable, String] =
    val initPayload = InitConnection(
      susertoken = encToken,
      t = "c",
      actid = s"${userId}_API",
      uid = s"${userId}_API",
      source = "API"
    ).toJson

    val outgoingStream = ZStream.fromQueue(outgoingQueue)
    val initStream     = ZStream.succeed(initPayload)

    // Combine init payload with user subscriptions
    val combinedOutgoing = initStream ++ outgoingStream

    val connect = basicRequest
      .response(asWebSocket(processWebSocket))
      .get(uri"$WebSocketUrl")
      .send(backend)
      .flatMap(_.body match
        case Right(stream) => ZIO.succeed(stream)
        case Left(err)     => ZIO.fail(new Exception(s"WebSocket connection failed: $err"))
      )

    ZStream.unwrap(connect)

  private def processWebSocket(ws: WebSocket[Task]): Task[ZStream[Any, Throwable, String]] =
    // Send Init
    val initPayload = InitConnection(
      susertoken = encToken,
      t = "c",
      actid = s"${userId}_API",
      uid = s"${userId}_API",
      source = "API"
    ).toJson

    for
      _ <- ws.sendText(initPayload)
      // Start a fiber to handle outgoing messages from the queue
      _ <- ZStream
        .fromQueue(outgoingQueue)
        .mapZIO(msg => ws.sendText(msg))
        .runDrain
        .forkDaemon
    yield ZStream.repeatZIO(ws.receiveText())

case class Instrument(exchange: String, token: String)
