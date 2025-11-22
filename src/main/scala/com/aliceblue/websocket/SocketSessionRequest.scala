package com.aliceblue.websocket

import zio.json._

case class SocketSessionRequest(loginType: String = "API")
object SocketSessionRequest:
  given JsonEncoder[SocketSessionRequest] = DeriveJsonEncoder.gen
