package com.aliceblue.models

import zio.json._

/** Response containing funds and margin information.
  * @param stat
  *   Status of the request.
  * @param cash
  *   Available cash.
  * @param payin
  *   Payin amount.
  * @param emsg
  *   Error message if any.
  */
case class FundsResponse(
    stat: String,
    cash: Option[String],
    payin: Option[String],
    emsg: Option[String]
)
object FundsResponse:
  given JsonDecoder[FundsResponse] = DeriveJsonDecoder.gen
