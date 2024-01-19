package escalade.sinks

import escalade.services.medianAmountSink
import escalade.utils.*
import munit.*
import zio.*
import zio.stream.ZStream

class MedianAmountSinkTests extends ZSuite {

  testZ("Median Sink should correctly compute median of an even-sized iterable") {
    val amount1 = 100000.0
    val amount2 = 300000.0
    val evenTransactions = Seq(10000.0, 20000.0, 30000.0, 40000.0) // Médiane = (20000 + 30000) / 2
    for {
      stream1 <- ZIO.succeed(ZStream.fromIterable(generateTransactions(n = 3, fixedAmount = Some(amount1))))
      stream2 <- ZIO.succeed(ZStream.fromIterable(generateTransactions(n = 3, fixedAmount = Some(amount2))))
      mergedStream = stream1.merge(stream2)
      result <- mergedStream.run(medianAmountSink)
      _ = assert(result == Some(((amount1 + amount2) / 2)))
    } yield ()
  }

  testZ("Median Sink should correctly compute median of an even-sized iterable") {
    val amount1 = 100000.0
    val amount2 = 300000.0
    val evenTransactions = Seq(10000.0, 20000.0, 30000.0, 40000.0)
    for {
      stream1 <- ZIO.succeed(ZStream.fromIterable(generateTransactions(n = 3, fixedAmount = Some(amount1))))
      stream2 <- ZIO.succeed(ZStream.fromIterable(generateTransactions(n = 2, fixedAmount = Some(amount2))))
      mergedStream = stream1.merge(stream2)
      result <- mergedStream.run(medianAmountSink)
      _ = assert(result == Some(amount1))
    } yield ()
  }

  testZ("Median Sink should return None if iterable is empty") {
    for {
      result <- ZStream.empty.run(medianAmountSink)
      _ = assert(result.isEmpty)
    } yield ()
  }
}
