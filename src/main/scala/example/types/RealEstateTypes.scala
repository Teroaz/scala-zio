package example.types

object RealEstateTypes {
  opaque type Category = String
  object Category {
    val valid_category = List("Appartement", "Maison")
    def apply(value: String): Option[Category] =
      Some(value).filter(isValidCategory)

    private def isValidCategory(value: String): Boolean = valid_category.contains(value)

    def value(cat: Category): String = cat
  }

  opaque type RoomCount = Int
  object RoomCount {
    def apply(value: Int): Option[RoomCount] =
      if (isValidRoomCount(value)) Some(value) else None

    def value(count: RoomCount): Int = count

    private def isValidRoomCount(value: Int): Boolean = value > 0
  }

  opaque type ConstructedArea = Int
  object ConstructedArea {
    def apply(value: Int): Option[ConstructedArea] =
      if (isValidArea(value)) Some(value) else None

    private def isValidArea(value: Int): Boolean = value > 0

    def value(area: ConstructedArea): Int = area

    extension (area: ConstructedArea) {
      def >(value: Int): Boolean = ConstructedArea.value(area) > value
    }
  }

  opaque type LandArea = Int
  object LandArea {
    def apply(value: Int): Option[LandArea] =
      if (isValidArea(value)) Some(value) else None

    private def isValidArea(value: Int): Boolean = value > 0

    def value(area: LandArea): Int = area
  }
}
