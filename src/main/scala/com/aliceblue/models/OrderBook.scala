package com.aliceblue.models

import zio.json._

/** Represents an item in the order book.
  * @param nOrdNo
  *   Order number.
  * @param prc
  *   Price.
  * @param qty
  *   Quantity.
  * @param pcode
  *   Product code.
  * @param prctyp
  *   Price type.
  * @param trantype
  *   Transaction type.
  * @param status
  *   Order status.
  */
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

/** Response containing the order book.
  * @param stat
  *   Status of the request.
  * @param emsg
  *   Error message if any.
  * @param result
  *   List of orders.
  */
case class OrderBookResponse(
    stat: String,
    emsg: Option[String],
    result: Option[List[OrderBookItem]]
)
object OrderBookResponse:
  given JsonDecoder[OrderBookResponse] = DeriveJsonDecoder.gen
