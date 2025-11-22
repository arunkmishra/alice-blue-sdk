package com.aliceblue.websocket

import zio.json._

case class InitConnection(susertoken: String, t: String, actid: String, uid: String, source: String)
object InitConnection:
  given JsonEncoder[InitConnection] = DeriveJsonEncoder.gen
