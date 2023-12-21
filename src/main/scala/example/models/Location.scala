package example.models

case class Location(
  number: Option[Int],
  suffix: Option[String],
  street: String,
  postalCode: Int,
  city: String,
  departmentCode: String,
  latitude: Double,
  longitude: Double
)