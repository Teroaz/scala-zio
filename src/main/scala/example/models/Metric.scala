package example.models

case class Metric(
                   averagePrice: Double,
                   averagePricePerSquareMeter: Double,
                   transactionCount: Long,
                   housingNatureDistribution: (Double, Double)
                 ) {
  override def toString: String = {
    val priceFormatted = f"$averagePrice%.2f"
    val pricePerSqMeterFormatted = f"$averagePricePerSquareMeter%.2f"
    val distributionFormatted = f"${housingNatureDistribution._1}%.2f%% (Maisons), ${housingNatureDistribution._2}%.2f%% (Appartements)"

    s"""
       |  averagePrice = $priceFormatted,
       |  averagePricePerSquareMeter = $pricePerSqMeterFormatted,
       |  transactionCount = $transactionCount,
       |  housingNatureDistribution = $distributionFormatted
      """.stripMargin
  }
}
