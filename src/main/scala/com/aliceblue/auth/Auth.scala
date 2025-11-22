package com.aliceblue.auth

import zio._
import com.aliceblue.models._
import com.aliceblue.client.ApiClient
import java.security.MessageDigest
import java.nio.charset.StandardCharsets

object Auth:

  /** Retrieves the encryption key for a user.
    *
    * @param userId
    *   The user ID.
    * @param apiClient
    *   The API client to use.
    * @return
    *   The encryption key.
    */
  def getEncryptionKey(userId: String, apiClient: ApiClient): Task[String] =
    apiClient
      .post[EncryptionKeyResponse, EncryptionKeyRequest]("customer/getAPIEncpkey", EncryptionKeyRequest(userId))
      .map(_.encKey)

  /** Retrieves the session ID.
    *
    * @param userId
    *   The user ID.
    * @param apiKey
    *   The API key.
    * @param encKey
    *   The encryption key.
    * @param apiClient
    *   The API client to use.
    * @return
    *   The session ID.
    */
  def getSessionId(
      userId: String,
      apiKey: String,
      encKey: String,
      apiClient: ApiClient
  ): Task[String] =
    val checksum = sha256(userId + apiKey + encKey)
    apiClient
      .post[SessionIdResponse, SessionIdRequest]("customer/getUserSID", SessionIdRequest(userId, checksum))
      .map(_.sessionID)

  private def sha256(input: String): String =
    val digest  = MessageDigest.getInstance("SHA-256")
    val encoded = digest.digest(input.getBytes(StandardCharsets.UTF_8))
    encoded.map("%02x".format(_)).mkString
