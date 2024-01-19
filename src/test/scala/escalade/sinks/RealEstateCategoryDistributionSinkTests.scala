package escalade.sinks

import escalade.models.*
import escalade.types.RealEstateTypes.*
import escalade.services.*
import escalade.utils.generateTransactions
import munit.*
import zio.*
import zio.stream.ZStream
class RealEstateCategoryDistributionSinkTests extends ZSuite {
  testZ("Distribution Sink should return distribution for each category") {
    val house_count = 10.0
    val appart_count = 5.0
    val total = house_count + appart_count
    for {
      stream1 <- ZIO.succeed(ZStream.fromIterable(generateTransactions(house_count.toInt, fixedCategory = Category("Maison"))))
      stream2 <- ZIO.succeed(ZStream.fromIterable(generateTransactions(appart_count.toInt, fixedCategory = Category("Appartement"))))
      mergedStream = stream1.merge(stream2)
      result <- mergedStream.run(realEstateCategoryDistributionSink)
      _ = assert(result == ((house_count/total)*100, (appart_count / total)*100), 0.01)
    } yield ()
  }

  testZ("Distribution Sink should return (0,0) if iterable is empty") {
    for {
      result <- ZStream.empty.run(realEstateCategoryDistributionSink)
      _ = assert(result == (0.0, 0.0))
    } yield ()
  }

}
