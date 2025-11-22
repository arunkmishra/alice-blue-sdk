package com.aliceblue.models

import zio.json._

/** Represents a trade.
  * @param fillId
  *   Fill ID.
  * @param qty
  *   Quantity.
  * @param price
  *   Price.
  * @param symbol
  *   Symbol.
  */
case class Trade(
    fillId: String,
    qty: String,
    price: String,
    symbol: String
)
object Trade:
  given JsonDecoder[Trade] = DeriveJsonDecoder.gen

/** Response containing the trade book.
  * @param stat
  *   Status of the request.
  * @param result
  *   List of trades.
  * @param emsg
  *   Error message if any.
  */
case class TradeBookResponse(
    stat: String,
    result: Option[List[Trade]],
    emsg: Option[String]
)
object TradeBookResponse:
  given JsonDecoder[TradeBookResponse] = DeriveJsonDecoder.gen
