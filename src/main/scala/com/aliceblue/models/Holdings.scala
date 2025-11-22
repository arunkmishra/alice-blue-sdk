package com.aliceblue.models

import zio.json._

/** Represents a holding in the portfolio.
  * @param isin
  *   ISIN of the scrip.
  * @param token
  *   Token of the scrip.
  * @param symbol
  *   Symbol name.
  * @param qty
  *   Quantity held.
  * @param price
  *   Average price.
  */
case class Holding(
    isin: String,
    token: String,
    symbol: String,
    qty: String,
    price: String
)
object Holding:
  given JsonDecoder[Holding] = DeriveJsonDecoder.gen

/** Response containing holdings.
  * @param stat
  *   Status of the request.
  * @param HoldingVal
  *   List of holdings.
  * @param emsg
  *   Error message if any.
  */
case class HoldingsResponse(
    stat: String,
    HoldingVal: Option[List[Holding]],
    emsg: Option[String]
)
object HoldingsResponse:
  given JsonDecoder[HoldingsResponse] = DeriveJsonDecoder.gen
