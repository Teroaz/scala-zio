package example.sinks

import example.avgSink
import example.utils.*
import munit.*
import zio.*
import zio.stream.ZStream
class AvgSinkTests extends ZSuite {
  testZ("Average Sink should return average of merged iterables") {
    val n = 5
    val amount1 = 20000.0
    val amount2 = 100000.0
    for {
      stream1 <- ZIO.succeed(ZStream.fromIterable(generateTransactions(n, fixedAmount = Some(amount1))))
      stream2 <- ZIO.succeed(ZStream.fromIterable(generateTransactions(n, fixedAmount = Some(amount2))))
      mergedStream = stream1.merge(stream2)
      result <- mergedStream.run(avgSink)
      _ = assert(result == ((amount1 * n + amount2 * n) / (n * 2)))
    } yield ()
  }

  testZ("Average Sink should return average of an iterable if many Transactions") {
    for {
      result <- ZStream.fromIterable(generateTransactions(n=1000000))
        .run(avgSink)
      _ = assert(result == 100000.0)
    } yield ()
  }

  testZ("Average Sink should return 0 if iterable is empty") {
    for {
      result <- ZStream.empty.run(avgSink)
      _ = assert(result == 0.0)
    } yield ()
  }
}

