package com.aliceblue

import zio._
import sttp.client3._
import sttp.client3.httpclient.zio.HttpClientZioBackend
import com.aliceblue.auth.Auth
import com.aliceblue.api.{Orders, Portfolio}
import com.aliceblue.models._

import com.aliceblue.websocket.{MarketData, MarketDataClient}
import sttp.capabilities.zio.ZioStreams
import sttp.capabilities.WebSockets

class AliceBlueClient(userId: String, apiKey: String, backend: SttpBackend[Task, ZioStreams & WebSockets]):
  private var sessionId: Option[String] = None

  def login: Task[String] =
    for
      encKey <- Auth.getEncryptionKey(userId, backend)
      sessId <- Auth.getSessionId(userId, apiKey, encKey, backend)
      _      <- ZIO.succeed { sessionId = Some(sessId) }
    yield sessId

  def getSessionId: Task[String] =
    ZIO.fromOption(sessionId).orElseFail(new Exception("Session ID not available. Please login first."))

  def placeOrder(request: PlaceOrderRequest): Task[PlaceOrderResponse] =
    getSessionId.flatMap(sessId => Orders.placeOrder(userId, sessId, request, backend))

  def cancelOrder(nestOrderNumber: String): Task[String] =
    getSessionId.flatMap(sessId => Orders.cancelOrder(userId, sessId, nestOrderNumber, backend))

  def getOrderBook: Task[List[OrderBookItem]] =
    getSessionId.flatMap(sessId => Orders.getOrderBook(userId, sessId, backend))

  def getHoldings: Task[List[Portfolio.Holding]] =
    getSessionId.flatMap(sessId => Portfolio.getHoldings(userId, sessId, backend))

  def getTradeBook: Task[List[Portfolio.Trade]] =
    getSessionId.flatMap(sessId => Portfolio.getTradeBook(userId, sessId, backend))

  def getFunds: Task[String] =
    getSessionId.flatMap(sessId => Portfolio.getFunds(userId, sessId, backend))

  def connectMarketData: Task[MarketDataClient] =
    getSessionId.flatMap(sessId => MarketData.connect(userId, sessId, backend))

object AliceBlueClient:
  def make(userId: String, apiKey: String): ZLayer[Any, Throwable, AliceBlueClient] =
    ZLayer.scoped {
      for backend <- HttpClientZioBackend.scoped()
      yield new AliceBlueClient(userId, apiKey, backend)
    }
