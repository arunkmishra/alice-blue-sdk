package com.aliceblue.models

import zio.json._

/** Enum representing order types.
  */
enum OrderType(val value: String):
  case Market         extends OrderType("MKT")
  case Limit          extends OrderType("LMT")
  case StopLoss       extends OrderType("SL")
  case StopLossMarket extends OrderType("SL-M")

object OrderType:
  given JsonEncoder[OrderType] = JsonEncoder[String].contramap(_.value)
  given JsonDecoder[OrderType] = JsonDecoder[String].map {
    case "MKT"  => OrderType.Market
    case "LMT"  => OrderType.Limit
    case "SL"   => OrderType.StopLoss
    case "SL-M" => OrderType.StopLossMarket
    case other  => throw new IllegalArgumentException(s"Unknown OrderType: $other")
  }
