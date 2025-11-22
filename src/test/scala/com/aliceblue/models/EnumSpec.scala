package com.aliceblue.models

import zio.test._
import zio.test.Assertion._
import zio.json._

object EnumSpec extends ZIOSpecDefault:
  def spec = suite("EnumSpec")(
    test("TransactionType serialization") {
      assertTrue(TransactionType.Buy.toJson == "\"BUY\"") &&
      assertTrue(TransactionType.Sell.toJson == "\"SELL\"")
    },
    test("TransactionType deserialization") {
      assertTrue("\"BUY\"".fromJson[TransactionType] == Right(TransactionType.Buy)) &&
      assertTrue("\"SELL\"".fromJson[TransactionType] == Right(TransactionType.Sell))
    },
    test("OrderType serialization") {
      assertTrue(OrderType.Market.toJson == "\"MKT\"") &&
      assertTrue(OrderType.Limit.toJson == "\"LMT\"")
    }
  )
