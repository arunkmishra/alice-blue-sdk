package com.aliceblue.models

import zio.json._

/** Request to place an order.
  * @param complexty
  *   Order complexity.
  * @param discqty
  *   Disclosed quantity.
  * @param exch
  *   Exchange.
  * @param pCode
  *   Product code.
  * @param prctyp
  *   Price type.
  * @param price
  *   Price.
  * @param qty
  *   Quantity.
  * @param ret
  *   Retention.
  * @param symbol_id
  *   Symbol ID.
  * @param trading_symbol
  *   Trading symbol.
  * @param transtype
  *   Transaction type.
  * @param trigPrice
  *   Trigger price.
  * @param orderTag
  *   Order tag.
  */
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

/** Response for order placement.
  * @param stat
  *   Status of the request.
  * @param nOrdNo
  *   Order number if successful.
  * @param emsg
  *   Error message if failed.
  */
case class PlaceOrderResponse(
    stat: String,
    nOrdNo: Option[String],
    emsg: Option[String]
)
object PlaceOrderResponse:
  given JsonDecoder[PlaceOrderResponse] = DeriveJsonDecoder.gen
