package escalade.types

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

  opaque type City = String

  object City {
    private val CityPattern = "^[a-zA-Z-éèàç ']+$".r

    def apply(name: String): Option[City] =
      if (isValid(name)) Some(name) else None

    private def isValid(name: String): Boolean = {
      CityPattern.matches(name)
    }

    private def levenshtein(s1: String, s2: String): Int = {
      val memo = Array.tabulate(s2.length + 1, s1.length + 1) { (i, j) => if (i == 0) j else if (j == 0) i else 0 }

      for {
        i <- 1 to s2.length
        j <- 1 to s1.length
      } memo(i)(j) = {
        if (s2(i - 1) == s1(j - 1)) memo(i - 1)(j - 1)
        else math.min(math.min(memo(i - 1)(j) + 1, memo(i)(j - 1) + 1), memo(i - 1)(j - 1) + 1)
      }

      memo(s2.length)(s1.length)
    }

    def isSimilar(city1: City, city2: City): Boolean = {
      val distance = levenshtein(city1, city2)
      val maxLength = math.max(city1.length, city2.length)
      val similarity = if (maxLength > 0) 1.0 - distance.toDouble / maxLength else 1.0
      similarity >= 0.85
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

    def compareCodes(firstCode: DepartmentCode, secondCode: DepartmentCode): Boolean = {
      def firstDigit(n: String): Int = n.take(1).toInt

      def firstTwoDigits(n: String): Int = n.take(2).toInt

      (firstCode.length, secondCode.length) match {
        case (4, 4) => firstDigit(firstCode) == firstDigit(secondCode)
        case (5, 5) => firstTwoDigits(firstCode) == firstTwoDigits(secondCode)
        case _ => false
      }
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