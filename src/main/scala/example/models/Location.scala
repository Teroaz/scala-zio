package example.models

import example.types.LocationTypes._
case class Location(
                     number: Option[Int],
                     suffix: Option[String],
                     street: String,
                     postalCode: PostalCode,
                     city: String,
                     departmentCode: DepartmentCode,
                     geoPoint: GeoPoint
                   )
