package example.models
import example.types.RealEstateTypes._
case class RealEstate(
                       category: Category,
                       rooms: RoomCount,
                       location: Location,
                       constructedArea: ConstructedArea,
                       landArea: LandArea
                     )
