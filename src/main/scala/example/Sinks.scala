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

val realEstateCategoryDistributionSink: ZSink[Any, Nothing, Transaction, Nothing, (Double, Double)] =
  ZSink.foldLeft[Transaction, (Double, Double)]((0.0, 0.0)) {
    case ((countMaisons, countAppartements), transaction) =>
      transaction.estate.category match {
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

val avgSink: ZSink[Any, Nothing, Transaction, Nothing, Double] =
  ZSink.foldLeft[Transaction, (Double, Int)]((0.0, 0)) {
    case ((total, count), transaction) =>
      (total + transaction.amount, count + 1)
  }.map {
    case (total, count) if count >0 => total / count
    case _ => 0.0
  }
