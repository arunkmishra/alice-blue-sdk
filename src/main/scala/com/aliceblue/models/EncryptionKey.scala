package com.aliceblue.models

import zio.json._

/** Request to get an encryption key.
  * @param userId
  *   The user ID.
  */
case class EncryptionKeyRequest(userId: String)
object EncryptionKeyRequest:
  given JsonEncoder[EncryptionKeyRequest] = DeriveJsonEncoder.gen

/** Response containing the encryption key.
  * @param encKey
  *   The encryption key.
  */
case class EncryptionKeyResponse(encKey: String)
object EncryptionKeyResponse:
  given JsonDecoder[EncryptionKeyResponse] = DeriveJsonDecoder.gen
