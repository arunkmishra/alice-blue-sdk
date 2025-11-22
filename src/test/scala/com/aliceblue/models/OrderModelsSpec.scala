package com.aliceblue.models

import zio.test._
import zio.test.Assertion._
import zio.json._

object OrderModelsSpec extends ZIOSpecDefault:
  def spec = suite("OrderModelsSpec")(
    test("PlaceOrderRequest serialization") {
      val req = PlaceOrderRequest(
        complexty = "regular",
        discqty = "0",
        exch = "NSE",
        pCode = "MIS",
        prctyp = "MKT",
        price = "0.0",
        qty = "1",
        ret = "DAY",
        symbol_id = "12345",
        trading_symbol = "ACC",
        transtype = "BUY",
        trigPrice = "0.0",
        orderTag = "tag"
      )
      assertTrue(req.toJson.contains("\"transtype\":\"BUY\""))
    }
  )
