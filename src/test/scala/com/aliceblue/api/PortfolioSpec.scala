package com.aliceblue.api

import zio._
import zio.test._
import zio.test.Assertion._
import sttp.client3._
import sttp.client3.testing.SttpBackendStub
import com.aliceblue.models._

import sttp.client3.httpclient.zio.HttpClientZioBackend

object PortfolioSpec extends ZIOSpecDefault:
  def spec = suite("PortfolioSpec")(
    test("getHoldings parses response correctly") {
      val backend = HttpClientZioBackend.stub
        .whenRequestMatches(_.uri.path.endsWith(List("positionAndHoldings", "holdings")))
        .thenRespond("""{"stat": "Ok", "HoldingVal": [{"isin": "ISIN1", "token": "1", "symbol": "TATA", "qty": "10", "price": "100"}]}""")

      for
        holdings <- Portfolio.getHoldings("USER1", "SESS1", backend)
      yield assertTrue(holdings.head.symbol == "TATA")
    },
    test("getTradeBook parses response correctly") {
      val backend = HttpClientZioBackend.stub
        .whenRequestMatches(_.uri.path.endsWith(List("placeOrder", "fetchTradeBook")))
        .thenRespond("""{"stat": "Ok", "result": [{"fillId": "1", "qty": "10", "price": "100", "symbol": "TATA"}]}""")

      for
        trades <- Portfolio.getTradeBook("USER1", "SESS1", backend)
      yield assertTrue(trades.head.symbol == "TATA")
    }
  )
