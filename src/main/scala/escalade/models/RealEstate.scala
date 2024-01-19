package escalade.models

import escalade.types.RealEstateTypes.*

case class RealEstate(
   category: Category,
   rooms: RoomCount,
   location: Location,
   constructedArea: ConstructedArea,
   landArea: LandArea
 )
