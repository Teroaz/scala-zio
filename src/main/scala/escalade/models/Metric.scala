package escalade.models

case class Metric(
                   averagePrice: Double,
                   averagePricePerSquareMeter: Double,
                   averageRoomCount: Double,
                   averageConstructedArea: Double,
                   averageLandArea: Double,
                   medianTransactionAmount: Option[Double],
                   transactionCount: Long,
                   housingNatureDistribution: (Double, Double)
                 ) {
  override def toString: String = {
    val priceFormatted = f"${averagePrice / 1000}%.0f kEUR"
    val pricePerSqMeterFormatted = f"$averagePricePerSquareMeter%.2f EUR/m2"
    val avgRoomCountFormatted = f"$averageRoomCount%.2f chambres"
    val avgConstructedAreaFormatted = f"$averageConstructedArea%.2f m2"
    val avgLandAreaFormatted = f"$averageLandArea%.2f m2"
    val medianTransactionAmountFormatted = medianTransactionAmount.map(amount => f"${amount / 1000}%.0f kEUR").getOrElse("N/A")
    val distributionFormatted = f"${housingNatureDistribution._1}%.2f%% Maisons, ${housingNatureDistribution._2}%.2f%% Appartements"

    s"""
       |Metrics:
       |  Transactions : $transactionCount,
       |  Average price = $priceFormatted,
       |  Average price/m2 = $pricePerSqMeterFormatted,
       |  Average rooms = $avgRoomCountFormatted,
       |  Average constructed area = $avgConstructedAreaFormatted,
       |  Average land Area = $avgLandAreaFormatted,
       |  Median transaction amount = $medianTransactionAmountFormatted,
       |  Housing nature distribution = $distributionFormatted
       """.stripMargin
  }
}
