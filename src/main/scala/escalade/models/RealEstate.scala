package escalade.models
import escalade.types.RealEstateTypes._
case class RealEstate(
                       category: Category,
                       rooms: RoomCount,
                       location: Location,
                       constructedArea: ConstructedArea,
                       landArea: LandArea
                     )
