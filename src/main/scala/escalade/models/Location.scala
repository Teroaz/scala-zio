package escalade.models

import escalade.types.LocationTypes.*

case class Location(
   number: Option[Int],
   suffix: Option[String],
   street: String,
   postalCode: PostalCode,
   city: City,
   departmentCode: DepartmentCode,
   geoPoint: GeoPoint
 )
