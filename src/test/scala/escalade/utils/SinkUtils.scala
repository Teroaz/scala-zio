package escalade.utils

import escalade.models.{Location, RealEstate, Transaction}
import escalade.types.LocationTypes.{City, DepartmentCode, GeoPoint, PostalCode}
import escalade.types.RealEstateTypes.{Category, ConstructedArea, LandArea, RoomCount}

import java.util.Date
import scala.util.Random

def generateTransactions(n: Int,
                         fixedAmount: Option[Double] = None,
                         fixedCA: Option[ConstructedArea] = None,
                         fixedCategory: Option[Category] = None
                        ): List[Transaction] = {
  (1 to n).map { _ =>
    Transaction(
      date = new Date(),
      nature = "Vente",
      amount = fixedAmount.getOrElse(100000.0),
      estate = RealEstate(
        category = fixedCategory.getOrElse(Category("Appartement").get),
        rooms = RoomCount(3).get,
        location = Location(
          number = Some(Random.nextInt(100)),
          suffix = Some("A"),
          street = "Rue de l'Exemple",
          postalCode = PostalCode("75000").get,
          city = City("Paris").get,
          departmentCode = DepartmentCode("75").get,
          geoPoint = GeoPoint(Random.nextDouble(), Random.nextDouble()).get
        ),
        constructedArea = fixedCA.getOrElse(ConstructedArea(100).get),
        landArea = LandArea(500).get
      )
    )
  }.toList
}
