package com.aliceblue.models

import zio.json._

enum TransactionType(val value: String):
  case Buy extends TransactionType("BUY")
  case Sell extends TransactionType("SELL")

object TransactionType:
  given JsonEncoder[TransactionType] = JsonEncoder[String].contramap(_.value)
  given JsonDecoder[TransactionType] = JsonDecoder[String].map {
    case "BUY" => TransactionType.Buy
    case "SELL" => TransactionType.Sell
    case other => throw new IllegalArgumentException(s"Unknown TransactionType: $other")
  }

enum OrderType(val value: String):
  case Market extends OrderType("MKT")
  case Limit extends OrderType("LMT")
  case StopLoss extends OrderType("SL")
  case StopLossMarket extends OrderType("SL-M")

object OrderType:
  given JsonEncoder[OrderType] = JsonEncoder[String].contramap(_.value)
  given JsonDecoder[OrderType] = JsonDecoder[String].map {
    case "MKT" => OrderType.Market
    case "LMT" => OrderType.Limit
    case "SL" => OrderType.StopLoss
    case "SL-M" => OrderType.StopLossMarket
    case other => throw new IllegalArgumentException(s"Unknown OrderType: $other")
  }

enum ProductType(val value: String):
  case Intraday extends ProductType("MIS")
  case Delivery extends ProductType("CNC")
  case Normal extends ProductType("NRML")
  case BracketOrder extends ProductType("BO")
  case CoverOrder extends ProductType("CO")

object ProductType:
  given JsonEncoder[ProductType] = JsonEncoder[String].contramap(_.value)
  given JsonDecoder[ProductType] = JsonDecoder[String].map {
    case "MIS" => ProductType.Intraday
    case "CNC" => ProductType.Delivery
    case "NRML" => ProductType.Normal
    case "BO" => ProductType.BracketOrder
    case "CO" => ProductType.CoverOrder
    case other => throw new IllegalArgumentException(s"Unknown ProductType: $other")
  }

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

// --- Auth Models ---

case class EncryptionKeyRequest(userId: String)
object EncryptionKeyRequest:
  given JsonEncoder[EncryptionKeyRequest] = DeriveJsonEncoder.gen

case class EncryptionKeyResponse(encKey: String)
object EncryptionKeyResponse:
  given JsonDecoder[EncryptionKeyResponse] = DeriveJsonDecoder.gen

case class SessionIdRequest(userId: String, userData: String)
object SessionIdRequest:
  given JsonEncoder[SessionIdRequest] = DeriveJsonEncoder.gen

case class SessionIdResponse(sessionID: String)
object SessionIdResponse:
  given JsonDecoder[SessionIdResponse] = DeriveJsonDecoder.gen

// --- Order Models ---

case class PlaceOrderRequest(
  complexty: String,
  discqty: String,
  exch: String,
  pCode: String,
  prctyp: String,
  price: String,
  qty: String,
  ret: String,
  symbol_id: String,
  trading_symbol: String,
  transtype: String,
  trigPrice: String,
  orderTag: String
)
object PlaceOrderRequest:
  given JsonEncoder[PlaceOrderRequest] = DeriveJsonEncoder.gen

case class PlaceOrderResponse(
  stat: String,
  nOrdNo: Option[String],
  emsg: Option[String]
)
object PlaceOrderResponse:
  given JsonDecoder[PlaceOrderResponse] = DeriveJsonDecoder.gen

case class OrderBookResponse(
  stat: String,
  emsg: Option[String],
  result: Option[List[OrderBookItem]]
)
object OrderBookResponse:
  given JsonDecoder[OrderBookResponse] = DeriveJsonDecoder.gen

case class OrderBookItem(
  nOrdNo: String,
  prc: String,
  qty: String,
  pcode: String,
  prctyp: String,
  trantype: String,
  status: String
)
object OrderBookItem:
  given JsonDecoder[OrderBookItem] = DeriveJsonDecoder.gen
