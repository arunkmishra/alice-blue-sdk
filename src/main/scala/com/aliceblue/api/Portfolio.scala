package com.aliceblue.api

import zio._
import zio.json._
import sttp.client3._
import sttp.client3.ziojson._
import com.aliceblue.models._

object Portfolio:
  private val BaseUrl = "https://ant.aliceblueonline.com/rest/AliceBlueAPIService/api"

  // --- Models ---
  case class Holding(
      isin: String,
      token: String,
      symbol: String,
      qty: String,
      price: String
  )
  object Holding:
    given JsonDecoder[Holding] = DeriveJsonDecoder.gen

  case class HoldingsResponse(
      stat: String,
      HoldingVal: Option[List[Holding]],
      emsg: Option[String]
  )
  object HoldingsResponse:
    given JsonDecoder[HoldingsResponse] = DeriveJsonDecoder.gen

  case class Trade(
      fillId: String,
      qty: String,
      price: String,
      symbol: String
  )
  object Trade:
    given JsonDecoder[Trade] = DeriveJsonDecoder.gen

  case class TradeBookResponse(
      stat: String,
      result: Option[List[Trade]],
      emsg: Option[String]
  )
  object TradeBookResponse:
    given JsonDecoder[TradeBookResponse] = DeriveJsonDecoder.gen

  case class FundsResponse(
      stat: String,
      cash: Option[String], // Need to verify field names
      payin: Option[String],
      emsg: Option[String]
  )
  object FundsResponse:
    // Since field names are unknown without checking pya3 response parsing or docs,
    // I will use a generic Map for now or try to infer from pya3 usage if possible.
    // pya3: fundsresp = self._get("fundsrecord")
    // It doesn't parse it in the snippet I saw.
    given JsonDecoder[FundsResponse] = DeriveJsonDecoder.gen

  // --- Implementation ---

  def getHoldings(
      userId: String,
      sessionId: String,
      backend: SttpBackend[Task, Any]
  ): Task[List[Holding]] =
    val request = basicRequest
      .get(uri"$BaseUrl/positionAndHoldings/holdings")
      .header("Authorization", s"Bearer $userId $sessionId")
      .response(asJson[HoldingsResponse])

    backend.send(request).flatMap { response =>
      response.body match
        case Right(success) if success.stat == "Ok" => ZIO.succeed(success.HoldingVal.getOrElse(Nil))
        case Right(failure)                         => ZIO.fail(new Exception(s"Holdings failed: ${failure.emsg}"))
        case Left(error)                            => ZIO.fail(new Exception(s"Failed to get holdings: $error"))
    }

  def getTradeBook(
      userId: String,
      sessionId: String,
      backend: SttpBackend[Task, Any]
  ): Task[List[Trade]] =
    val request = basicRequest
      .get(uri"$BaseUrl/placeOrder/fetchTradeBook")
      .header("Authorization", s"Bearer $userId $sessionId")
      .response(asJson[TradeBookResponse])

    backend.send(request).flatMap { response =>
      response.body match
        case Right(success) if success.stat == "Ok" => ZIO.succeed(success.result.getOrElse(Nil))
        case Right(failure)                         => ZIO.fail(new Exception(s"Trade book failed: ${failure.emsg}"))
        case Left(error)                            => ZIO.fail(new Exception(s"Failed to get trade book: $error"))
    }

  def getFunds(
      userId: String,
      sessionId: String,
      backend: SttpBackend[Task, Any]
  ): Task[String] =
    // Returning raw JSON string for funds as structure is uncertain
    val request = basicRequest
      .get(uri"$BaseUrl/limits/getRmsLimits")
      .header("Authorization", s"Bearer $userId $sessionId")
      .response(asString)

    backend.send(request).flatMap { response =>
      response.body match
        case Right(success) => ZIO.succeed(success)
        case Left(error)    => ZIO.fail(new Exception(s"Failed to get funds: $error"))
    }
