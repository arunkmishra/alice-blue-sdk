# Alice Blue Scala ZIO SDK

A functional, type-safe Scala SDK for the Alice Blue trading platform, built using [ZIO](https://zio.dev) and [sttp](https://sttp.softwaremill.com).

## Features

- **Pure Functional API**: Built on ZIO for robust error handling and concurrency.
- **Type-Safe Models**: Comprehensive case classes and enums for API requests and responses.
- **WebSocket Support**: Real-time market data streaming using ZIO Streams.
- **Portfolio Management**: Easy access to holdings, funds, and trade book.
- **Order Management**: Place, cancel, and modify orders with ease.

## Installation

Add the following dependency to your `build.sbt`:

```scala
libraryDependencies += "io.github.arunkmishra" %% "alice-blue-sdk" % "0.1.0"
```

Or for Maven:

```xml
<dependency>
    <groupId>io.github.arunkmishra</groupId>
    <artifactId>alice-blue-sdk_3</artifactId>
    <version>0.1.0</version>
</dependency>
```

## Getting Started

### 1. Initialize the Client

Create an instance of `AliceBlueClient` using `ZLayer`.

```scala
import zio._
import com.aliceblue.AliceBlueClient

object MyApp extends ZIOAppDefault:
  override def run =
    val program = for
      client <- ZIO.service[AliceBlueClient]
      sessionId <- client.login
      _ <- Console.printLine(s"Logged in with Session ID: $sessionId")
    yield ()

    program.provide(
      AliceBlueClient.make("YOUR_USER_ID", "YOUR_API_KEY")
    )
```

### 2. Place an Order

```scala
import com.aliceblue.models._

val orderRequest = PlaceOrderRequest(
  complexty = "regular",
  discqty = "0",
  exch = "NSE",
  pCode = "MIS", // Intraday
  prctyp = "MKT", // Market Order
  price = "0.0",
  qty = "1",
  ret = "DAY",
  symbol_id = "26000", // Nifty
  trading_symbol = "NIFTY",
  transtype = "BUY",
  trigPrice = "0.0",
  orderTag = "my_strategy"
)

val response = client.placeOrder(orderRequest)
```

### 3. Stream Market Data

```scala
import com.aliceblue.websocket.Instrument

val instrument = Instrument("NSE", "26000") // Nifty

for
  marketData <- client.connectMarketData
  _ <- marketData.subscribe(List(instrument))
  _ <- marketData.stream
        .tap(msg => Console.printLine(s"Market Data: $msg"))
        .runDrain
yield ()
```

### 4. Fetch Portfolio

```scala
for
  holdings <- client.getHoldings
  _ <- Console.printLine(s"Holdings: $holdings")
  
  funds <- client.getFunds
  _ <- Console.printLine(s"Funds: $funds")
yield ()
```

## Project Structure

- `com.aliceblue.AliceBlueClient`: Main entry point.
- `com.aliceblue.models`: Request/Response models and Enums.
- `com.aliceblue.api`: Core API logic (Orders, Portfolio).
- `com.aliceblue.websocket`: WebSocket client for market data.

## License

Apache 2.0
