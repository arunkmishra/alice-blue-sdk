package com.aliceblue.api

import zio._
import zio.json._
import sttp.client3._
import sttp.client3.ziojson._
import com.aliceblue.models._

object Orders:
  private val BaseUrl = "https://ant.aliceblueonline.com/rest/AliceBlueAPIService/api"

  def placeOrder(
    userId: String,
    sessionId: String,
    request: PlaceOrderRequest,
    backend: SttpBackend[Task, Any]
  ): Task[PlaceOrderResponse] =
    val authenticatedRequest = basicRequest
      .post(uri"$BaseUrl/placeOrder/executePlaceOrder")
      .header("Authorization", s"Bearer $userId $sessionId")
      .header("Content-Type", "application/json")
      .body(List(request).toJson) // API expects a list
      .response(asJson[List[PlaceOrderResponse]])

    backend.send(authenticatedRequest).flatMap { response =>
      response.body match
        case Right(List(success: PlaceOrderResponse)) => ZIO.succeed(success)
        case Right(list: List[PlaceOrderResponse]) if list.nonEmpty => ZIO.succeed(list.head) // Handle multiple?
        case Right(Nil) => ZIO.fail(new Exception("Empty response from placeOrder"))
        case Left(error) => ZIO.fail(new Exception(s"Failed to place order: $error"))
    }

  def cancelOrder(
    userId: String,
    sessionId: String,
    nestOrderNumber: String,
    backend: SttpBackend[Task, Any]
  ): Task[String] =
    // pya3: data = {'nestOrderNumber': nestordernmbr}
    // cancelresp = self._post("cancelorder", data)
    // "cancelorder": "placeOrder/cancelOrder"
    
    case class CancelRequest(nestOrderNumber: String)
    given JsonEncoder[CancelRequest] = DeriveJsonEncoder.gen
    
    // Response is likely a generic status response, need to check model.
    // For now returning raw string or we can define a generic response.
    
    val request = basicRequest
      .post(uri"$BaseUrl/placeOrder/cancelOrder")
      .header("Authorization", s"Bearer $userId $sessionId")
      .body(CancelRequest(nestOrderNumber))
      .response(asString)

    backend.send(request).flatMap { response =>
      response.body match
        case Right(success) => ZIO.succeed(success)
        case Left(error) => ZIO.fail(new Exception(s"Failed to cancel order: $error"))
    }

  def getOrderBook(
    userId: String,
    sessionId: String,
    backend: SttpBackend[Task, Any]
  ): Task[List[OrderBookItem]] =
    val request = basicRequest
      .get(uri"$BaseUrl/placeOrder/fetchOrderBook")
      .header("Authorization", s"Bearer $userId $sessionId")
      .response(asJson[OrderBookResponse])

    backend.send(request).flatMap { response =>
      response.body match
        case Right(success) if success.stat == "Ok" => ZIO.succeed(success.result.getOrElse(Nil))
        case Right(failure) => ZIO.fail(new Exception(s"Order book failed: ${failure.emsg}"))
        case Left(error) => ZIO.fail(new Exception(s"Failed to get order book: $error"))
    }
