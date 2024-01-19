package example.services

import example.models.UserFilters
import zio.Console.readLine
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
  maxAmount <- readLine("Entrez le montant maximun (ou laissez vide) : ").map(_.toIntOption)
  propertyType <- readLine("Entrez le type de bien (appartement/maison) (ou laissez vide) : ")
} yield UserFilters(year, minAmount, maxAmount, if (propertyType.isEmpty) None else Some(propertyType))
