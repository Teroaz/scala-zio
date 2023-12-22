package example.models

// TODO : Add Validation & constraints with NewTypes & OpaqueTypes
// TODO : Convert latitude & longitude to a GeoPoint type
case class Location(
  number: Option[Int],
  suffix: Option[String],
  street: String,
  postalCode: String,
  city: String,
  departmentCode: String,
  latitude: Double,
  longitude: Double
)