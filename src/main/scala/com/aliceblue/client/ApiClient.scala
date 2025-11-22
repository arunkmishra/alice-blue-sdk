package com.aliceblue.client

import zio._
import zio.json._
import sttp.client3._
import sttp.client3.ziojson._

/** A trait representing an API client for making HTTP requests. Abstracts away the underlying HTTP client
  * implementation.
  */
trait ApiClient {

  /** Performs a GET request.
    *
    * @param path
    *   The API endpoint path.
    * @param headers
    *   Optional headers to include in the request.
    * @tparam T
    *   The type to decode the response body into.
    * @return
    *   A Task containing the decoded response.
    */
  def get[T: JsonDecoder](path: String, headers: Map[String, String] = Map.empty): Task[T]

  /** Performs a POST request with a JSON body.
    *
    * @param path
    *   The API endpoint path.
    * @param body
    *   The request body to serialize to JSON.
    * @param headers
    *   Optional headers to include in the request.
    * @tparam T
    *   The type to decode the response body into.
    * @tparam B
    *   The type of the request body.
    * @return
    *   A Task containing the decoded response.
    */
  def post[T: JsonDecoder, B: JsonEncoder](path: String, body: B, headers: Map[String, String] = Map.empty): Task[T]

  /** Performs a POST request without a body.
    *
    * @param path
    *   The API endpoint path.
    * @param headers
    *   Optional headers to include in the request.
    * @tparam T
    *   The type to decode the response body into.
    * @return
    *   A Task containing the decoded response.
    */
  def postNoBody[T: JsonDecoder](path: String, headers: Map[String, String] = Map.empty): Task[T]

  /** Performs a POST request and expects a raw string response.
    *
    * @param path
    *   The API endpoint path.
    * @param body
    *   The request body to serialize to JSON.
    * @param headers
    *   Optional headers to include in the request.
    * @tparam B
    *   The type of the request body.
    * @return
    *   A Task containing the raw string response.
    */
  def postString[B: JsonEncoder](path: String, body: B, headers: Map[String, String] = Map.empty): Task[String]
}

case class AliceBlueApiClient(
    baseUrl: String,
    userId: String,
    sessionId: Option[String],
    backend: SttpBackend[Task, Any]
) extends ApiClient {

  private def commonHeaders(headers: Map[String, String]): Map[String, String] = {
    val authHeader = sessionId.map(sid => "Authorization" -> s"Bearer $userId $sid").toMap
    authHeader ++ headers
  }

  override def get[T: JsonDecoder](path: String, headers: Map[String, String]): Task[T] = {
    val request = basicRequest
      .get(uri"$baseUrl".addPath(path.split('/').toSeq))
      .headers(commonHeaders(headers))
      .response(asJson[T])

    backend.send(request).flatMap { response =>
      response.body match {
        case Right(success) => ZIO.succeed(success)
        case Left(error)    => ZIO.fail(new Exception(s"GET $path failed: $error"))
      }
    }
  }

  override def post[T: JsonDecoder, B: JsonEncoder](path: String, body: B, headers: Map[String, String]): Task[T] = {
    val request = basicRequest
      .post(uri"$baseUrl".addPath(path.split('/').toSeq))
      .headers(commonHeaders(headers))
      .body(body)
      .response(asJson[T])

    backend.send(request).flatMap { response =>
      response.body match {
        case Right(success) => ZIO.succeed(success)
        case Left(error)    => ZIO.fail(new Exception(s"POST $path failed: $error"))
      }
    }
  }

  override def postNoBody[T: JsonDecoder](path: String, headers: Map[String, String]): Task[T] = {
    val request = basicRequest
      .post(uri"$baseUrl".addPath(path.split('/').toSeq))
      .headers(commonHeaders(headers))
      .response(asJson[T])

    backend.send(request).flatMap { response =>
      response.body match {
        case Right(success) => ZIO.succeed(success)
        case Left(error)    => ZIO.fail(new Exception(s"POST $path failed: $error"))
      }
    }
  }

  override def postString[B: JsonEncoder](path: String, body: B, headers: Map[String, String]): Task[String] = {
    val request = basicRequest
      .post(uri"$baseUrl".addPath(path.split('/').toSeq))
      .headers(commonHeaders(headers))
      .body(body)
      .response(asString)

    backend.send(request).flatMap { response =>
      response.body match {
        case Right(success) => ZIO.succeed(success)
        case Left(error)    => ZIO.fail(new Exception(s"POST $path failed: $error"))
      }
    }
  }
}
