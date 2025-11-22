package com.aliceblue.models

import zio.json._

/** Enum representing transaction types (Buy/Sell).
  */
enum TransactionType(val value: String):
  case Buy  extends TransactionType("BUY")
  case Sell extends TransactionType("SELL")

object TransactionType:
  given JsonEncoder[TransactionType] = JsonEncoder[String].contramap(_.value)
  given JsonDecoder[TransactionType] = JsonDecoder[String].map {
    case "BUY"  => TransactionType.Buy
    case "SELL" => TransactionType.Sell
    case other  => throw new IllegalArgumentException(s"Unknown TransactionType: $other")
  }
