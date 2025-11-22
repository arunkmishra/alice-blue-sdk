package com.aliceblue.websocket

import zio.json._

case class SubscriptionRequest(k: String, t: String)
object SubscriptionRequest:
  given JsonEncoder[SubscriptionRequest] = DeriveJsonEncoder.gen
