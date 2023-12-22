package example.models

// TODO : Add Validation & constraints with NewTypes & OpaqueTypes
case class RealEstate(
  category: String,
  rooms: Option[Int],
  location: Location,
  constructedArea: Option[Int],
  landArea: Option[Int],
)
