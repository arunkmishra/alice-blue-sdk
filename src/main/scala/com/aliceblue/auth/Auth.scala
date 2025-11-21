package com.aliceblue.auth

import zio._
import sttp.client3._
import sttp.client3.ziojson._
import com.aliceblue.models._
import java.security.MessageDigest
import java.nio.charset.StandardCharsets

object Auth:
  private val BaseUrl = "https://ant.aliceblueonline.com/rest/AliceBlueAPIService/api"

  def getEncryptionKey(userId: String, backend: SttpBackend[Task, Any]): Task[String] =
    val request = basicRequest
      .post(uri"$BaseUrl/customer/getAPIEncpkey")
      .body(EncryptionKeyRequest(userId))
      .response(asJson[EncryptionKeyResponse])

    backend.send(request).flatMap { response =>
      response.body match
        case Right(success) => ZIO.succeed(success.encKey)
        case Left(error) => ZIO.fail(new Exception(s"Failed to get encryption key: $error"))
    }

  def getSessionId(userId: String, apiKey: String, encKey: String, backend: SttpBackend[Task, Any]): Task[String] =
    val checksum = sha256(userId + apiKey + encKey)
    val request = basicRequest
      .post(uri"$BaseUrl/customer/getUserSID")
      .body(SessionIdRequest(userId, checksum))
      .response(asJson[SessionIdResponse])

    backend.send(request).flatMap { response =>
      response.body match
        case Right(success) => ZIO.succeed(success.sessionID)
        case Left(error) => ZIO.fail(new Exception(s"Failed to get session ID: $error"))
    }

  private def sha256(input: String): String =
    val digest = MessageDigest.getInstance("SHA-256")
    val encoded = digest.digest(input.getBytes(StandardCharsets.UTF_8))
    encoded.map("%02x".format(_)).mkString
