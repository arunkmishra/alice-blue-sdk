package com.aliceblue.models

import zio.json._

/** Enum representing supported exchanges.
  */
enum Exchange(val value: String):
  case NSE extends Exchange("NSE")
  case BSE extends Exchange("BSE")
  case NFO extends Exchange("NFO")
  case MCX extends Exchange("MCX")
  case CDS extends Exchange("CDS")

object Exchange:
  given JsonEncoder[Exchange] = JsonEncoder[String].contramap(_.value)
  given JsonDecoder[Exchange] = JsonDecoder[String].map {
    case "NSE" => Exchange.NSE
    case "BSE" => Exchange.BSE
    case "NFO" => Exchange.NFO
    case "MCX" => Exchange.MCX
    case "CDS" => Exchange.CDS
    case other => throw new IllegalArgumentException(s"Unknown Exchange: $other")
  }
