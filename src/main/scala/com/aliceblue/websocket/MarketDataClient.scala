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

/** Client for interacting with the Market Data WebSocket.
  *
  * @param userId
  *   The user ID.
  * @param encToken
  *   The encryption token for the WebSocket session.
  * @param outgoingQueue
  *   Queue for sending messages to the WebSocket.
  * @param backend
  *   The SttpBackend used for the connection.
  */
class MarketDataClient(
    userId: String,
    encToken: String,
    outgoingQueue: Queue[String],
    backend: SttpBackend[Task, ZioStreams & WebSockets]
):
  import MarketData._

  /** Subscribes to market data for the given instruments.
    *
    * @param instruments
    *   The list of instruments to subscribe to.
    * @return
    *   A Task representing the subscription action.
    */
  def subscribe(instruments: List[Instrument]): Task[Unit] =
    val scripts = instruments.map(i => s"${i.exchange}|${i.token}").mkString("#")
    val req     = SubscriptionRequest(k = scripts, t = "t") // t=t for token, t=d for depth
    outgoingQueue.offer(req.toJson).unit

  /** Unsubscribes from market data for the given instruments.
    *
    * @param instruments
    *   The list of instruments to unsubscribe from.
    * @return
    *   A Task representing the unsubscription action.
    */
  def unsubscribe(instruments: List[Instrument]): Task[Unit] =
    val scripts = instruments.map(i => s"${i.exchange}|${i.token}").mkString("#")
    val req     = SubscriptionRequest(k = scripts, t = "u")
    outgoingQueue.offer(req.toJson).unit

  private val initPayloadJson: String =
    InitConnection(
      susertoken = encToken,
      t = "c",
      actid = s"${userId}_API",
      uid = s"${userId}_API",
      source = "API"
    ).toJson

  // Stream of raw JSON messages for now. Can be parsed into MarketDataUpdate objects.
  /** A stream of raw JSON messages received from the WebSocket.
    */
  val stream: ZStream[Any, Throwable, String] =
    val outgoingStream = ZStream.fromQueue(outgoingQueue)
    val initStream     = ZStream.succeed(initPayloadJson)

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
    for
      _ <- ws.sendText(initPayloadJson)
      // Start a fiber to handle outgoing messages from the queue
      _ <- ZStream
        .fromQueue(outgoingQueue)
        .mapZIO(msg => ws.sendText(msg))
        .runDrain
        .forkDaemon
    yield ZStream.repeatZIO(ws.receiveText())
