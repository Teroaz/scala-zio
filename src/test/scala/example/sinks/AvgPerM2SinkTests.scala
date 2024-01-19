package example.sinks

import example.models.*
import example.types.RealEstateTypes.*
import example.services.*
import example.utils.generateTransactions
import munit.*
import zio.*
import zio.stream.ZStream

class AvgPerM2SinkTests extends ZSuite {

  testZ("AvgPerM2 Sink should return average amount per square meter") {
    val amount1 = 500000.0
    val amount2 = 300000.0
    val area = 50
    val n = 5
    for {
      stream1 <- ZIO.succeed(ZStream.fromIterable(generateTransactions(n, fixedAmount = Some(amount1), fixedCA = ConstructedArea(area))))
      stream2 <- ZIO.succeed(ZStream.fromIterable(generateTransactions(n, fixedAmount = Some(amount2), fixedCA = ConstructedArea(area))))
      mergedStream = stream1.merge(stream2)
        result <- mergedStream.run(avgPerM2Sink)
      _ = assert(result == ((n * (amount1 / area) + n * (amount2 / area)) / (n * 2)))
    } yield ()
  }

  testZ("AvgPerM2 Sink should return 0 for empty stream") {
    for {
      result <- ZStream.empty.run(avgPerM2Sink)
      _ = assert(result == 0.0)
    } yield ()
  }
}

