package com.aliceblue

import zio.*
import zio.stream.*
import com.aliceblue.models.*
import com.aliceblue.websocket.Instrument

import java.io.IOException

object Main extends ZIOAppDefault:
  override def run: ZIO[ZIOAppArgs & Scope, IOException, Unit] =
    val program = for
      userId <- System.env("ALICE_BLUE_USER_ID").someOrFail(new Exception("ALICE_BLUE_USER_ID not set"))
      apiKey <- System.env("ALICE_BLUE_API_KEY").someOrFail(new Exception("ALICE_BLUE_API_KEY not set"))
      
      client <- ZIO.service[AliceBlueClient]
      
      _ <- Console.printLine(s"Logging in user: $userId")
      sessionId <- client.login
      _ <- Console.printLine(s"Logged in successfully. Session ID: $sessionId")
      
      // Example: Get Order Book
      // _ <- client.getOrderBook.tap(ob => Console.printLine(s"Order Book: $ob"))

      // Example: Connect Market Data
      _ <- Console.printLine("Connecting to Market Data...")
      marketData <- client.connectMarketData
      
      // Subscribe to Nifty Bank (Example token, replace with valid one)
      // You need to know the exchange and token.
      // val instrument = Instrument("NSE", "26009") // Nifty Bank
      // _ <- marketData.subscribe(List(instrument))
      
      _ <- Console.printLine("Listening for market data...")
      _ <- marketData.stream
            .tap(msg => Console.printLine(s"Received: $msg"))
            .take(10) // Take 10 messages then exit
            .runDrain
            
    yield ()

    program.provide(
      AliceBlueClient.make(
        sys.env.getOrElse("ALICE_BLUE_USER_ID", ""),
        sys.env.getOrElse("ALICE_BLUE_API_KEY", "")
      )
    ).catchAll(e => Console.printLine(s"Error: ${e.getMessage}"))
