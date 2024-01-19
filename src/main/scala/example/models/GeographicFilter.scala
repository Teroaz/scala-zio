package example.models

import example.types.LocationTypes._

sealed trait GeographicFilter

object GeographicFilter {
  final case class CityFilter(city: City) extends GeographicFilter
  final case class DepartmentFilter(department: DepartmentCode) extends GeographicFilter
}
