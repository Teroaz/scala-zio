package example

import example.models.Transaction
import zio.stream.ZSink

val avgPerM2Sink: ZSink[Any, Nothing, Transaction, Nothing, Double] =
  ZSink.foldLeft[Transaction, (Double, Int)]((0.0, 0)) {
      case ((total, count), transaction) =>
        transaction.estate.constructedArea match {
          case Some(area) if area > 0 => (total + (transaction.amount / area), count + 1)
          case _ => (total, count)
        }

    }
    .map {
      case (total, count) if count > 0 => total / count
      case _ => 0.0
    }