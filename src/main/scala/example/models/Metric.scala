package example.models

case class Metric (
  averagePrice: Double,
  averagePricePerSquareMeter: Double,
  transactionCount: Long,
  housingNatureDistribution: (Double, Double)
){
  override def toString: String = {
    s"Metric(averagePrice = $averagePrice, averagePricePerSquareMeter = $averagePricePerSquareMeter, " +
      s"transactionCount = $transactionCount, housingNatureDistribution = $housingNatureDistribution)"
  }
}
