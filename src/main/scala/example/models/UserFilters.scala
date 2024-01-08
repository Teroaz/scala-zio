package example.models

case class UserFilters(
  year: Option[Int],
  minAmount: Option[Int],
  maxAmount: Option[Int],
  propertyType: Option[String],
//  minRooms: Option[Int],
//  maxRooms: Option[Int],
)