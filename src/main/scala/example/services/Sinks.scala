package example.services

import example.models.{Metric, Transaction}
import example.types.RealEstateTypes.{Category, ConstructedArea, LandArea, RoomCount}
import zio.ZIO
import zio.stream.{ZSink, ZStream}

/**
 * A ZSink that calculates the average price per square meter of real estate transactions.
 *
 * This sink processes each `Transaction` to compute the price per square meter, summing these
 * values and counting the number of valid transactions (where area is greater than 0). It then
 * calculates the average price per square meter across all transactions.
 *
 * Output:
 * - Returns the average price per square meter as a `Double`.
 * - Returns 0.0 if there are no valid transactions.
 */
val avgPerM2Sink: ZSink[Any, Nothing, Transaction, Nothing, Double] =
  def amountPerArea(amount: Double, area: Double): Double = {
    amount / area
  }

  ZSink.foldLeft[Transaction, (Double, Int)]((0.0, 0)) {
    case ((total, count), transaction) =>
      val area = ConstructedArea.value(transaction.estate.constructedArea)
      if (area > 0) (total + amountPerArea(transaction.amount,area), count + 1)
      else (total, count)
  }.map {
    case (total, count) if count > 0 => total / count
    case _ => 0.0
  }

/**
 * A ZSink that calculates the distribution of real estate transactions by category (Maisons, Appartements).
 *
 * This sink processes each `Transaction`, counting the number of transactions that are categorized
 * as "Maison" and "Appartement". It calculates the percentage distribution of these two categories
 * over the total number of transactions.
 *
 * Output:
 * - Returns a tuple `(Double, Double)`, where the first Double represents the percentage of "Maison"
 * transactions and the second Double represents the percentage of "Appartement" transactions.
 * - Returns (0.0, 0.0) if there are no transactions.
 */
val realEstateCategoryDistributionSink: ZSink[Any, Nothing, Transaction, Nothing, (Double, Double)] =
  ZSink.foldLeft[Transaction, (Double, Double)]((0.0, 0.0)) {
    case ((countMaisons, countAppartements), transaction) =>
      Category.value(transaction.estate.category) match {
        case "Maison" => (countMaisons + 1, countAppartements)
        case "Appartement" => (countMaisons, countAppartements + 1)
        case _ => (countMaisons, countAppartements)
      }
  }.map {
    case (countMaisons, countAppartements) =>
      val total = countMaisons + countAppartements
      if (total > 0)
        ((countMaisons / total) * 100, (countAppartements / total) * 100)
      else
        (0.0, 0.0)
  }


def amountPerArea(amount: Double, area: Double): Double = amount / area

def avgSink(getValue: Transaction => Double): ZSink[Any, Nothing, Transaction, Nothing, Double] =
  ZSink.foldLeft[Transaction, (Double, Int)]((0.0, 0)) {
    case ((total, count), transaction) =>
      (total + getValue(transaction), count + 1)
  }.map {
    case (total, count) if count > 0 => total / count
    case _ => 0.0
  }

val avgAmountSink = avgSink(_.amount)
val avgRoomSink = avgSink(transaction => RoomCount.value(transaction.estate.rooms))
val avgConstructedAreaSink = avgSink(transaction => ConstructedArea.value(transaction.estate.constructedArea))
val avgLandAreaSink = avgSink(transaction => LandArea.value(transaction.estate.landArea))

val medianAmountSink: ZSink[Any, Nothing, Transaction, Nothing, Option[Double]] =
  ZSink.collectAll[Transaction].map { transactions =>
    if (transactions.isEmpty) None
    else {
      val amounts = transactions.map(_.amount).sorted
      val n = amounts.length
      if (n % 2 == 0) Some((amounts(n / 2 - 1) + amounts(n / 2)) / 2.0)
      else Some(amounts(n / 2))
    }
  }


/**
 * Computes various metrics for a stream of real estate transactions.
 *
 * This function uses `ZStream.broadcast` to duplicate the incoming `transactionStream` into four separate streams,
 * each processed by a different sink:
 * - `avgPerM2Sink`: Calculates average price per square meter.
 * - `realEstateCategoryDistributionSink`: Calculates the distribution of transactions by property category.
 * - `avgSink`: Calculates the average transaction amount.
 * - `ZSink.runCount`: Counts the total number of transactions.
 *
 * These metrics are then aggregated into a `Metric` object.
 *
 * @param transactionStream A stream of `Transaction` objects to be processed.
 * @return A `ZIO` effect that, when executed, will yield a `Metric` object containing the calculated metrics.
 *
 *         Example:
 * {{{
 *   val transactionStream: ZStream[Any, Throwable, Transaction] = ...
 *   val metrics: ZIO[Any, Throwable, Metric] = computeMetrics(transactionStream)
 *   metrics.map { metric =>
 *     // Process the calculated metrics
 *   }
 * }}}
 */
def computeMetrics(transactionStream: ZStream[Any, Throwable, Transaction]): ZIO[Any, Throwable, Metric] = {
  ZIO.scoped {
    transactionStream.broadcast(8, 16).flatMap { streams =>
      for {
        avgPerM2Fiber <- streams(0).run(avgPerM2Sink).forkScoped
        distribFiber <- streams(1).run(realEstateCategoryDistributionSink).forkScoped
        avgFiber <- streams(2).run(avgAmountSink).forkScoped
        countFiber <- streams(3).runCount.forkScoped
        avgRoomFiber <- streams(4).run(avgRoomSink).forkScoped
        avgConstructedAreaFiber <- streams(5).run(avgConstructedAreaSink).forkScoped
        avgLandAreaFiber <- streams(6).run(avgLandAreaSink).forkScoped
        medianTransactionAmountFiber <- streams(7).run(medianAmountSink).forkScoped
        avgPerM2 <- avgPerM2Fiber.join
        distrib <- distribFiber.join
        avg <- avgFiber.join
        count <- countFiber.join
        avgRoom <- avgRoomFiber.join
        avgConstructedArea <- avgConstructedAreaFiber.join
        avgLandArea <- avgLandAreaFiber.join
        medianTransactionAmount <- medianTransactionAmountFiber.join
      } yield Metric(
        averagePrice = avg,
        averagePricePerSquareMeter = avgPerM2,
        averageRoomCount = avgRoom,
        averageConstructedArea = avgConstructedArea,
        averageLandArea = avgLandArea,
        medianTransactionAmount = medianTransactionAmount,
        transactionCount = count,
        housingNatureDistribution = distrib
      )
    }
  }
}
