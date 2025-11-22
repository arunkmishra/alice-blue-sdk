package com.aliceblue.websocket

import zio.json._

case class SocketSessionResponse(stat: String, emsg: Option[String])
object SocketSessionResponse:
  given JsonDecoder[SocketSessionResponse] = DeriveJsonDecoder.gen
