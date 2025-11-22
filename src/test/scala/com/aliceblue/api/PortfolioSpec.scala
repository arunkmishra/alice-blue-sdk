package com.aliceblue.api

import zio._
import zio.test._
import zio.test.Assertion._
import sttp.client3._
import sttp.client3.testing.SttpBackendStub
import com.aliceblue.models._
import com.aliceblue.client.AliceBlueApiClient
import sttp.client3.httpclient.zio.HttpClientZioBackend

object PortfolioSpec extends ZIOSpecDefault:
  def spec = suite("PortfolioSpec")(
    test("getHoldings parses response correctly") {
      val backend = HttpClientZioBackend.stub
        .whenRequestMatches(_.uri.path.endsWith(List("positionAndHoldings", "holdings")))
        .thenRespond(
          """{"stat": "Ok", "HoldingVal": [{"isin": "ISIN1", "token": "1", "symbol": "TATA", "qty": "10", "price": "100"}]}"""
        )

      val apiClient = AliceBlueApiClient("http://test", "USER1", Some("SESS1"), backend)

      for holdings <- Portfolio.getHoldings(apiClient)
      yield assertTrue(holdings.head.symbol == "TATA")
    },
    test("getTradeBook parses response correctly") {
      val backend = HttpClientZioBackend.stub
        .whenRequestMatches(_.uri.path.endsWith(List("placeOrder", "fetchTradeBook")))
        .thenRespond("""{"stat": "Ok", "result": [{"fillId": "1", "qty": "10", "price": "100", "symbol": "TATA"}]}""")

      val apiClient = AliceBlueApiClient("http://test", "USER1", Some("SESS1"), backend)

      for trades <- Portfolio.getTradeBook(apiClient)
      yield assertTrue(trades.head.symbol == "TATA")
    },
    test("getFunds parses response correctly") {
      val backend = HttpClientZioBackend.stub
        .whenRequestMatches(_.uri.path.endsWith(List("limits", "getRmsLimits")))
        .thenRespond("""{"stat": "Ok", "cash": "1000.0", "payin": "0.0"}""")

      val apiClient = AliceBlueApiClient("http://test", "USER1", Some("SESS1"), backend)

      for funds <- Portfolio.getFunds(apiClient)
      yield assertTrue(funds.cash.contains("1000.0"))
    }
  )
