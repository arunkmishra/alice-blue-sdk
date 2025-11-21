# Alice Blue Scala ZIO SDK

[![Maven Central](https://img.shields.io/maven-central/v/io.github.arunkmishra/alice-blue-sdk_3.svg)](https://central.sonatype.com/artifact/io.github.arunkmishra/alice-blue-sdk_3)
[![License](https://img.shields.io/badge/License-Apache%202.0-blue.svg)](https://opensource.org/licenses/Apache-2.0)

A functional, type-safe Scala SDK for the [Alice Blue](https://aliceblueonline.com/) trading platform, built using [ZIO](https://zio.dev) and [sttp](https://sttp.softwaremill.com).

## Features

- **Pure Functional API**: Built on ZIO for robust error handling and concurrency.
- **Type-Safe Models**: Comprehensive case classes and enums for API requests and responses.
- **WebSocket Support**: Real-time market data streaming using ZIO Streams.
- **Portfolio Management**: Easy access to holdings, funds, and trade book.
- **Order Management**: Place, cancel, and modify orders with ease.

## Installation

### Prerequisites
- Java 11 or higher
- Scala 3.3.x

### SBT
Add the following dependency to your `build.sbt`:

```scala
libraryDependencies += "io.github.arunkmishra" %% "alice-blue-sdk" % "0.1.0"
```

### Maven
```xml
<dependency>
    <groupId>io.github.arunkmishra</groupId>
    <artifactId>alice-blue-sdk_3</artifactId>
    <version>0.1.0</version>
</dependency>
```

## Getting Started

### 1. Initialize the Client

Create an instance of `AliceBlueClient` using `ZLayer`. You will need your User ID and API Key.

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

### 2. Master Contracts

Fetch tradable instruments and contracts.

```scala
// Fetch all NSE Equity contracts
val contracts = client.getMasterContract("NSE")
```

### 3. Place an Order

Place a buy or sell order.

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

### 4. Modify an Order

```scala
val modifyRequest = ModifyOrderRequest(
  nestOrderNumber = "2103170000001",
  exch = "NSE",
  prctyp = "LMT",
  price = "15000.0",
  qty = "1",
  trigPrice = "0.0",
  trading_symbol = "NIFTY",
  discqty = "0"
)

val response = client.modifyOrder(modifyRequest)
```

### 5. Cancel an Order

```scala
val response = client.cancelOrder("NSE", "2103170000001")
```

### 6. Stream Market Data

Connect to the WebSocket to receive real-time ticks.

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

### 7. Fetch Portfolio

Access your holdings and funds.

```scala
for
  holdings <- client.getHoldings
  _ <- Console.printLine(s"Holdings: $holdings")
  
  funds <- client.getFunds
  _ <- Console.printLine(s"Funds: $funds")
yield ()
```

## Development

### Build
To build the project from source:

```bash
sbt compile
```

### Test
To run the unit tests:

```bash
sbt test
```

### Run
To run the example application (if configured):

```bash
sbt run
```

## License

Apache 2.0
