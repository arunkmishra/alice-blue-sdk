package com.aliceblue

import zio._
import sttp.client3._
import sttp.client3.httpclient.zio.HttpClientZioBackend
import com.aliceblue.auth.Auth
import com.aliceblue.api.{Orders, Portfolio}
import com.aliceblue.models._
import com.aliceblue.client.{AliceBlueApiClient, ApiClient}

import com.aliceblue.websocket.{MarketData, MarketDataClient}
import sttp.capabilities.zio.ZioStreams
import sttp.capabilities.WebSockets

/** Client for interacting with the Alice Blue API.
  *
  * @param userId
  *   The user ID (e.g., "AB1234").
  * @param apiKey
  *   The API key generated from the Alice Blue portal.
  * @param backend
  *   The SttpBackend to use for HTTP requests.
  */
class AliceBlueClient(userId: String, apiKey: String, backend: SttpBackend[Task, ZioStreams & WebSockets]):
  private var sessionId: Option[String] = None
  private val baseUrl                   = "https://ant.aliceblueonline.com/rest/AliceBlueAPIService/api"

  private def createApiClient(sid: Option[String]): ApiClient =
    AliceBlueApiClient(baseUrl, userId, sid, backend)

  /** Logs in to the Alice Blue API.
    *
    * This method performs a two-step authentication:
    *   1. Gets an encryption key using the user ID. 2. Gets a session ID using the user ID, API key, and encryption
    *      key.
    *
    * @return
    *   The session ID.
    */
  def login: Task[String] =
    val noAuthClient = createApiClient(None)
    for
      encKey <- Auth.getEncryptionKey(userId, noAuthClient)
      sessId <- Auth.getSessionId(userId, apiKey, encKey, noAuthClient)
      _      <- ZIO.succeed { sessionId = Some(sessId) }
    yield sessId

  /** Gets the current session ID.
    *
    * @return
    *   The session ID if logged in, otherwise fails with an exception.
    */
  def getSessionId: Task[String] =
    ZIO.fromOption(sessionId).orElseFail(new Exception("Session ID not available. Please login first."))

  private def authenticatedClient: Task[ApiClient] =
    getSessionId.map(sid => createApiClient(Some(sid)))

  /** Places an order.
    *
    * @param request
    *   The order placement request details.
    * @return
    *   The response containing the order status and order number.
    */
  def placeOrder(request: PlaceOrderRequest): Task[PlaceOrderResponse] =
    authenticatedClient.flatMap(client => Orders.placeOrder(request, client))

  /** Cancels an order.
    *
    * @param nestOrderNumber
    *   The order number to cancel.
    * @return
    *   A string indicating the cancellation status.
    */
  def cancelOrder(nestOrderNumber: String): Task[String] =
    authenticatedClient.flatMap(client => Orders.cancelOrder(nestOrderNumber, client))

  /** Retrieves the order book.
    *
    * @return
    *   A list of orders in the order book.
    */
  def getOrderBook: Task[List[OrderBookItem]] =
    authenticatedClient.flatMap(client => Orders.getOrderBook(client))

  /** Retrieves the current holdings.
    *
    * @return
    *   A list of holdings.
    */
  def getHoldings: Task[List[Holding]] =
    authenticatedClient.flatMap(client => Portfolio.getHoldings(client))

  /** Retrieves the trade book.
    *
    * @return
    *   A list of trades.
    */
  def getTradeBook: Task[List[Trade]] =
    authenticatedClient.flatMap(client => Portfolio.getTradeBook(client))

  /** Retrieves the funds and margin information.
    *
    * @return
    *   The funds response containing cash and payin details.
    */
  def getFunds: Task[FundsResponse] =
    authenticatedClient.flatMap(client => Portfolio.getFunds(client))

  /** Connects to the Market Data WebSocket.
    *
    * @return
    *   A `MarketDataClient` instance for subscribing to market data.
    */
  def connectMarketData: Task[MarketDataClient] =
    for
      sessId <- getSessionId
      client <- authenticatedClient
      md     <- MarketData.connect(userId, sessId, client, backend)
    yield md

object AliceBlueClient:
  /** Creates a ZLayer for `AliceBlueClient`.
    *
    * @param userId
    *   The user ID.
    * @param apiKey
    *   The API key.
    * @return
    *   A ZLayer that provides an `AliceBlueClient`.
    */
  def make(userId: String, apiKey: String): ZLayer[Any, Throwable, AliceBlueClient] =
    ZLayer.scoped {
      for backend <- HttpClientZioBackend.scoped()
      yield new AliceBlueClient(userId, apiKey, backend)
    }
