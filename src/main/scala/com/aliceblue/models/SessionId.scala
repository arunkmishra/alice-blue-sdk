package com.aliceblue.models

import zio.json._

/** Request to get a session ID.
  * @param userId
  *   The user ID.
  * @param userData
  *   The checksum (SHA256 of userId + apiKey + encKey).
  */
case class SessionIdRequest(userId: String, userData: String)
object SessionIdRequest:
  given JsonEncoder[SessionIdRequest] = DeriveJsonEncoder.gen

/** Response containing the session ID.
  * @param sessionID
  *   The session ID.
  */
case class SessionIdResponse(sessionID: String)
object SessionIdResponse:
  given JsonDecoder[SessionIdResponse] = DeriveJsonDecoder.gen
