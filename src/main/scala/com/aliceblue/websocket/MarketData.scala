package com.aliceblue.websocket

import zio._
import zio.stream._
import sttp.client3._
import sttp.capabilities.WebSockets
import sttp.capabilities.zio.ZioStreams
import com.aliceblue.client.ApiClient
import java.security.MessageDigest
import java.nio.charset.StandardCharsets

/** Object for managing Market Data WebSocket connections.
  */
object MarketData:
  val WebSocketUrl = "wss://ws1.aliceblueonline.com/NorenWS/"

  // --- Implementation ---

  /** Connects to the Market Data WebSocket.
    *
    * @param userId
    *   The user ID.
    * @param sessionId
    *   The session ID.
    * @param apiClient
    *   The API client to use for session creation.
    * @param backend
    *   The SttpBackend to use for the WebSocket connection.
    * @return
    *   A Task containing the connected `MarketDataClient`.
    */
  def connect(
      userId: String,
      sessionId: String,
      apiClient: ApiClient,
      backend: SttpBackend[Task, ZioStreams & WebSockets]
  ): Task[MarketDataClient] =
    for
      _        <- prepareSocketSession(userId, sessionId, apiClient)
      encToken <- ZIO.attempt(generateEncToken(sessionId))
      queue    <- Queue.unbounded[String] // Queue for outgoing messages (subscriptions)
    // We return a client that can be used to subscribe/unsubscribe
    // The actual connection is managed in a background fiber or stream
    yield new MarketDataClient(userId, encToken, queue, backend)

  private def prepareSocketSession(userId: String, sessionId: String, apiClient: ApiClient): Task[Unit] =
    // Invalidate existing session
    val invalidate =
      apiClient.post[SocketSessionResponse, SocketSessionRequest]("ws/invalidateSocketSess", SocketSessionRequest())

    // Create new session
    val create =
      apiClient.post[SocketSessionResponse, SocketSessionRequest]("ws/createSocketSess", SocketSessionRequest())

    for
      _          <- invalidate // Ignore result as per pya3? Or check stat? pya3 checks stat.
      createResp <- create
      _ <-
        if (createResp.stat == "Ok") ZIO.unit
        else ZIO.fail(new Exception(s"Failed to create socket session: ${createResp.emsg}"))
    yield ()

  private def generateEncToken(sessionId: String): String =
    val sha256_1 = sha256(sessionId)
    sha256(sha256_1)

  private def sha256(input: String): String =
    val digest  = MessageDigest.getInstance("SHA-256")
    val encoded = digest.digest(input.getBytes(StandardCharsets.UTF_8))
    encoded.map("%02x".format(_)).mkString
