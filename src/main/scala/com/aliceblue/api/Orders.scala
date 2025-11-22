package com.aliceblue.api

import zio._
import com.aliceblue.models._
import com.aliceblue.client.ApiClient

object Orders:

  /** Places an order.
    *
    * @param request
    *   The order placement request details.
    * @param apiClient
    *   The API client to use.
    * @return
    *   The response containing the order status and order number.
    */
  def placeOrder(
      request: PlaceOrderRequest,
      apiClient: ApiClient
  ): Task[PlaceOrderResponse] =
    // API expects a list for placeOrder
    apiClient
      .post[List[PlaceOrderResponse], List[PlaceOrderRequest]]("placeOrder/executePlaceOrder", List(request))
      .flatMap {
        case success :: _ => ZIO.succeed(success)
        case Nil          => ZIO.fail(new Exception("Empty response from placeOrder"))
      }

  /** Cancels an order.
    *
    * @param nestOrderNumber
    *   The order number to cancel.
    * @param apiClient
    *   The API client to use.
    * @return
    *   A string indicating the cancellation status.
    */
  def cancelOrder(
      nestOrderNumber: String,
      apiClient: ApiClient
  ): Task[String] =
    apiClient.postString[CancelRequest]("placeOrder/cancelOrder", CancelRequest(nestOrderNumber))

  /** Retrieves the order book.
    *
    * @param apiClient
    *   The API client to use.
    * @return
    *   A list of orders in the order book.
    */
  def getOrderBook(
      apiClient: ApiClient
  ): Task[List[OrderBookItem]] =
    apiClient.get[OrderBookResponse]("placeOrder/fetchOrderBook").flatMap { response =>
      if (response.stat == "Ok") ZIO.succeed(response.result.getOrElse(Nil))
      else ZIO.fail(new Exception(s"Order book failed: ${response.emsg}"))
    }
