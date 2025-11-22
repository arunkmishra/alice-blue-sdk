package com.aliceblue.api

import zio._
import com.aliceblue.models._
import com.aliceblue.client.ApiClient

object Portfolio:

  /** Retrieves the current holdings.
    *
    * @param apiClient
    *   The API client to use.
    * @return
    *   A list of holdings.
    */
  def getHoldings(apiClient: ApiClient): Task[List[Holding]] =
    apiClient.get[HoldingsResponse]("positionAndHoldings/holdings").flatMap { response =>
      if (response.stat == "Ok") ZIO.succeed(response.HoldingVal.getOrElse(Nil))
      else ZIO.fail(new Exception(s"Holdings failed: ${response.emsg}"))
    }

  /** Retrieves the trade book.
    *
    * @param apiClient
    *   The API client to use.
    * @return
    *   A list of trades.
    */
  def getTradeBook(apiClient: ApiClient): Task[List[Trade]] =
    apiClient.get[TradeBookResponse]("placeOrder/fetchTradeBook").flatMap { response =>
      if (response.stat == "Ok") ZIO.succeed(response.result.getOrElse(Nil))
      else ZIO.fail(new Exception(s"Trade book failed: ${response.emsg}"))
    }

  /** Retrieves the funds and margin information.
    *
    * @param apiClient
    *   The API client to use.
    * @return
    *   The funds response containing cash and payin details.
    */
  def getFunds(apiClient: ApiClient): Task[FundsResponse] =
    apiClient.get[FundsResponse]("limits/getRmsLimits")
