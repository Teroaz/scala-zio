package escalade.services

import escalade.models.GeographicFilter.{CityFilter, DepartmentFilter}
import escalade.models.{GeographicFilter, UserFilters}
import escalade.types.LocationTypes.{City, DepartmentCode}
import zio.Console.{printLine, readLine}
import zio.ZIO

import java.io.IOException

/**
 * Prompts the user for property transaction filters.
 *
 * This function collects user input for filtering property transactions based on criteria like year, amount,
 * and property type. Empty inputs are treated as None. The function returns a ZIO effect with UserFilters
 * or fails with an IOException.
 *
 * @return A ZIO effect that yields UserFilters with user-provided criteria or an IOException on error.
 */
def getUserFilters: ZIO[Any, IOException, UserFilters] = for {
  year <- readLine("Entrez l'année de la transaction (ou laissez vide) : ").map(_.toIntOption)
  minAmount <- readLine("Entrez le montant minimum (ou laissez vide) : ").map(_.toIntOption)
  maxAmount <- readLine("Entrez le montant maximum (ou laissez vide) : ").map(_.toIntOption)
  propertyType <- readLine("Entrez le type de bien (appartement/maison) (ou laissez vide) : ")

  _ <- printLine("Voulez-vous appliquer un filtre géographique ? (oui/non)")
  geographicChoice <- readLine
  geographicFilter <- if (geographicChoice.toLowerCase == "oui") getGeographicFilters else ZIO.succeed(None)
} yield UserFilters(year, minAmount, maxAmount, if (propertyType.isEmpty) None else Some(propertyType), geographicFilter)


/**
 * Collects geographic filters for property transactions.
 *
 * This function presents the user with options to choose a geographic filter, either by city or department.
 * Depending on the user's choice, it prompts for additional input such as city name or department code.
 * The resulting geographic filter is wrapped in an `Option`.
 *
 * @return A ZIO effect that yields an `Option[GeographicFilter]` representing the selected geographic filter
 *         or None if no filter is chosen. The effect can fail with an IOException.
 */
def getGeographicFilters: ZIO[Any, IOException, Option[GeographicFilter]] = {
  def selectFilter: ZIO[Any, IOException, Option[GeographicFilter]] = for {
    _ <- printLine("Choisissez un filtre : 1 - Ville, 2 - Département")
    choice <- readLine
    filter <- choice match {
      case "1" =>
        readLine("Entrez le nom de la ville : ")
          .map(City(_))
          .map(optCity => optCity.map(city => CityFilter.apply(city)))

      case "2" =>
        readLine("Entrez le code du département : ")
          .map(DepartmentCode(_))
          .map(optDeptCode => optDeptCode.map(deptCode => DepartmentFilter.apply(deptCode)))

      case _ =>
        printLine("Choix invalide. Veuillez réessayer.").flatMap(_ => selectFilter)
    }
  } yield filter

  selectFilter
}