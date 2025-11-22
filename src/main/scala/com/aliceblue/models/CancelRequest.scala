package com.aliceblue.models

import zio.json._

/** Request to cancel an order.
  * @param nestOrderNumber
  *   The order number to cancel.
  */
case class CancelRequest(nestOrderNumber: String)
object CancelRequest:
  given JsonEncoder[CancelRequest] = DeriveJsonEncoder.gen
