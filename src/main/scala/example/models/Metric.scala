package example.models

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
    val priceFormatted = f"$averagePrice%.2f"
    val pricePerSqMeterFormatted = f"$averagePricePerSquareMeter%.2f"
    val avgRoomCountFormatted = f"$averageRoomCount%.2f"
    val avgConstructedAreaFormatted = f"$averageConstructedArea%.2f"
    val avgLandAreaFormatted = f"$averageLandArea%.2f"
    val medianTransactionAmountFormatted = medianTransactionAmount.map(amount => f"$amount%.2f").getOrElse("N/A")
    val distributionFormatted = f"${housingNatureDistribution._1}%.2f%% (Maisons), ${housingNatureDistribution._2}%.2f%% (Appartements)"

    s"""
       |  averagePrice = $priceFormatted,
       |  averagePricePerSquareMeter = $pricePerSqMeterFormatted,
       |  averageRoomCount = $avgRoomCountFormatted,
       |  averageConstructedArea = $avgConstructedAreaFormatted,
       |  averageLandArea = $avgLandAreaFormatted,
       |  medianTransactionAmount = $medianTransactionAmountFormatted,
       |  transactionCount = $transactionCount,
       |  housingNatureDistribution = $distributionFormatted
          """.stripMargin
  }
}
