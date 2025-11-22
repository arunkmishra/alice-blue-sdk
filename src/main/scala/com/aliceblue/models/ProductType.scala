package com.aliceblue.models

import zio.json._

/** Enum representing product types.
  */
enum ProductType(val value: String):
  case Intraday     extends ProductType("MIS")
  case Delivery     extends ProductType("CNC")
  case Normal       extends ProductType("NRML")
  case BracketOrder extends ProductType("BO")
  case CoverOrder   extends ProductType("CO")

object ProductType:
  given JsonEncoder[ProductType] = JsonEncoder[String].contramap(_.value)
  given JsonDecoder[ProductType] = JsonDecoder[String].map {
    case "MIS"  => ProductType.Intraday
    case "CNC"  => ProductType.Delivery
    case "NRML" => ProductType.Normal
    case "BO"   => ProductType.BracketOrder
    case "CO"   => ProductType.CoverOrder
    case other  => throw new IllegalArgumentException(s"Unknown ProductType: $other")
  }
