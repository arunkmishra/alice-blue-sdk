package com.aliceblue.api

import zio._
import zio.test._
import zio.test.Assertion._
import sttp.client3._
import sttp.client3.testing.SttpBackendStub
import com.aliceblue.models._

import sttp.client3.httpclient.zio.HttpClientZioBackend

object OrdersSpec extends ZIOSpecDefault:
  def spec = suite("OrdersSpec")(
    test("placeOrder sends correct request and parses response") {
      val backend = HttpClientZioBackend.stub
        .whenRequestMatches(_.uri.path.endsWith(List("placeOrder", "executePlaceOrder")))
        .thenRespond("""[{"stat": "Ok", "nOrdNo": "12345"}]""")

      val req = PlaceOrderRequest(
        complexty = "regular", discqty = "0", exch = "NSE", pCode = "MIS",
        prctyp = "MKT", price = "0.0", qty = "1", ret = "DAY",
        symbol_id = "1", trading_symbol = "TATA", transtype = "BUY",
        trigPrice = "0.0", orderTag = "tag"
      )

      for
        resp <- Orders.placeOrder("USER1", "SESS1", req, backend)
      yield assertTrue(resp.stat == "Ok") && assertTrue(resp.nOrdNo.contains("12345"))
    },
    test("cancelOrder sends correct request") {
      val backend = HttpClientZioBackend.stub
        .whenRequestMatches(_.uri.path.endsWith(List("placeOrder", "cancelOrder")))
        .thenRespond("Cancelled")

      for
        resp <- Orders.cancelOrder("USER1", "SESS1", "12345", backend)
      yield assertTrue(resp == "Cancelled")
    },
    test("getOrderBook parses response correctly") {
      val backend = HttpClientZioBackend.stub
        .whenRequestMatches(_.uri.path.endsWith(List("placeOrder", "fetchOrderBook")))
        .thenRespond("""{"stat": "Ok", "result": [{"nOrdNo": "1", "prc": "100", "qty": "1", "pcode": "MIS", "prctyp": "MKT", "trantype": "BUY", "status": "COMPLETE"}]}""")

      for
        book <- Orders.getOrderBook("USER1", "SESS1", backend)
      yield assertTrue(book.head.nOrdNo == "1")
    }
  )
