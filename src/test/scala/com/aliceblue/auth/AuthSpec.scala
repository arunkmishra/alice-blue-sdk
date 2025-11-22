package com.aliceblue.auth

import zio._
import zio.test._
import zio.test.Assertion._
import sttp.client3._
import sttp.client3.testing.SttpBackendStub
import sttp.model.StatusCode
import com.aliceblue.models._
import com.aliceblue.client.AliceBlueApiClient
import sttp.client3.httpclient.zio.HttpClientZioBackend

object AuthSpec extends ZIOSpecDefault:
  def spec = suite("AuthSpec")(
    test("getEncryptionKey returns key on success") {
      val backend = HttpClientZioBackend.stub
        .whenRequestMatches(_.uri.path.endsWith(List("customer", "getAPIEncpkey")))
        .thenRespond("""{"encKey": "testKey"}""")

      val apiClient = AliceBlueApiClient("http://test", "USER1", None, backend)

      for key <- Auth.getEncryptionKey("USER1", apiClient)
      yield assertTrue(key == "testKey")
    },
    test("getSessionId returns session ID on success") {
      val backend = HttpClientZioBackend.stub
        .whenRequestMatches(_.uri.path.endsWith(List("customer", "getUserSID")))
        .thenRespond("""{"sessionID": "sess123"}""")

      val apiClient = AliceBlueApiClient("http://test", "USER1", None, backend)

      for sess <- Auth.getSessionId("USER1", "APIKEY", "ENCKEY", apiClient)
      yield assertTrue(sess == "sess123")
    }
  )
