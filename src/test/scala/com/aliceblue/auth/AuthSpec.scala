package com.aliceblue.auth

import zio._
import zio.test._
import zio.test.Assertion._
import sttp.client3._
import sttp.client3.testing.SttpBackendStub
import sttp.model.StatusCode
import com.aliceblue.models._

import sttp.client3.httpclient.zio.HttpClientZioBackend

object AuthSpec extends ZIOSpecDefault:
  def spec = suite("AuthSpec")(
    test("getEncryptionKey returns key on success") {
      val backend = HttpClientZioBackend.stub
        .whenRequestMatches(_.uri.path.endsWith(List("customer", "getAPIEncpkey")))
        .thenRespond("""{"encKey": "testKey"}""")

      for
        key <- Auth.getEncryptionKey("USER1", backend)
      yield assertTrue(key == "testKey")
    },
    test("getSessionId returns session ID on success") {
      val backend = HttpClientZioBackend.stub
        .whenRequestMatches(_.uri.path.endsWith(List("customer", "getUserSID")))
        .thenRespond("""{"sessionID": "sess123"}""")

      for
        sess <- Auth.getSessionId("USER1", "APIKEY", "ENCKEY", backend)
      yield assertTrue(sess == "sess123")
    }
  )
