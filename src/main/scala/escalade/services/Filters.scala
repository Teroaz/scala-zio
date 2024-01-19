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
  year <- readLine("Enter the transaction year (or leave empty): ").map(_.toIntOption)
  minAmount <- readLine("Enter the minimum amount (or leave empty): ").map(_.toIntOption)
  maxAmount <- readLine("Enter the maximum amount (or leave empty): ").map(_.toIntOption)
  propertyType <- readLine("Enter the property type (apartment/house) (or leave empty): ")

  _ <- printLine("Do you want to apply a geographic filter? (yes/no)")
  geographicChoice <- readLine
  geographicFilter <- if (geographicChoice.toLowerCase == "yes") getGeographicFilters else ZIO.succeed(None)
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
    _ <- printLine("Choose a filter: 1 - City, 2 - Department (4 or 5 digits)")
    choice <- readLine
    filter <- choice match {
      case "1" =>
        readLine("Enter the city name: ")
          .map(City(_))
          .map(optCity => optCity.map(city => CityFilter.apply(city)))

      case "2" =>
        readLine("Enter the department code: ")
          .map(DepartmentCode(_))
          .map(optDeptCode => optDeptCode.map(deptCode => DepartmentFilter.apply(deptCode)))

      case _ =>
        printLine("Invalid choice. Please try again.").flatMap(_ => selectFilter)
    }
  } yield filter

  selectFilter
}
