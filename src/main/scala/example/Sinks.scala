package example

import example.models.Transaction
import zio.stream.ZSink
import example.types.RealEstateTypes._

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