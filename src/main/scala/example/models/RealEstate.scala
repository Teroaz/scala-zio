package example.models

case class RealEstate(
  category: String,
  rooms: Option[Int],
  location: Location,
  constructedArea: Option[Int],
  landArea: Option[Int],
)
