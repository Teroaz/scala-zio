package escalade.services

import escalade.models.GeographicFilter.{CityFilter, DepartmentFilter}
import escalade.models.{GeographicFilter, UserFilters}
import escalade.types.LocationTypes.{City, DepartmentCode}
import zio.Console.{printLine, readLine}
import zio.ZIO

import java.io.IOException

/**
 * Retrieves user input to create a set of user filters for property transactions.
 *
 * This function prompts the user to enter various criteria related to property transactions:
 * - Year of the transaction
 * - Minimum transaction amount
 * - Maximum transaction amount
 * - Type of property (e.g., apartment, house)
 *
 * The user can leave any of these inputs empty if they do not wish to apply that particular filter.
 *
 * The function uses a for-comprehension to sequentially read and process user inputs. Inputs for
 * year, minimum amount, and maximum amount are expected to be integers and are converted to `Option[Int]`.
 * An empty input results in `None`. For the property type, the input is directly processed into an `Option[String]`,
 * with an empty input resulting in `None`.
 *
 * @return A `ZIO` effect that, when executed, will either yield `UserFilters` with the provided criteria
 *         or fail with an `IOException` if an error occurs during input reading.
 *
 *         Example:
 * {{{
 *   val userFilters: ZIO[Any, IOException, UserFilters] = getUserFilters
 *   userFilters.map { filters =>
 *     // Use filters for processing
 *   }
 * }}}
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