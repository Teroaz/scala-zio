package example.types

object LocationTypes {
  opaque type PostalCode = String

  object PostalCode {
    private val PostalCodePattern = "^\\d{4,5}$".r

    def apply(code: String): Option[PostalCode] =
      if (isValid(code)) Some(code) else None

    private def isValid(code: String): Boolean = {
      PostalCodePattern.matches(code)
    }
  }

  opaque type DepartmentCode = String

  object DepartmentCode {
    private val DepartmentCodePattern = "^(\\d{2,3}|2[AB])$".r

    def apply(code: String): Option[DepartmentCode] =
      if (isValid(code)) Some(code) else None

    private def isValid(code: String): Boolean = {
      DepartmentCodePattern.matches(code)
    }
  }

  opaque type GeoPoint = (Double, Double)

  object GeoPoint {
    def apply(latitude: Double, longitude: Double): Option[GeoPoint] =
      if (isValid(latitude, longitude)) Some((latitude, longitude)) else None

    private def isValid(latitude: Double, longitude: Double): Boolean = {
      latitude >= -50 && latitude <= 51 && longitude >= -61 && longitude <= 77
    }
  }
}