package example.services

import example.EnvVars
import example.models.{GeographicFilter, Transaction, UserFilters}
import zio.{Console, Scope, ZIO}
import zio.http.Client
import zio.stream.{ZPipeline, ZStream}
import example.helpers.{decompressGzippedData, parseCsvLine}
import example.helpers.Http.fetchData
import example.types.LocationTypes.{DepartmentCode, City}
import example.types.RealEstateTypes.Category

/**
 * Validates a real estate transaction based on certain criteria.
 *
 * This function checks if a given transaction meets all of the following conditions:
 * - The transaction amount must be greater than 0.
 * - The estate's category must be either "Appartement" or "Maison".
 * - The nature of the transaction must be "Vente" (indicating a sale).
 * - The constructed area of the estate must be greater than 0.
 *
 * If all these conditions are met, the transaction is considered valid.
 *
 * @param transaction The `Transaction` object to be validated.
 * @return `true` if the transaction meets all the validation criteria, otherwise `false`.
 *
 *         Example:
 * {{{
 *   val transaction = Transaction(...)
 *   val isValid = validateTransaction(transaction)
 *   if (isValid) {
 *     // Proceed with the transaction
 *   } else {
 *     // Handle invalid transaction
 *   }
 * }}}
 */
def validateTransaction(transaction: Transaction): Boolean = {
  transaction.amount > 0 &&
    (transaction.estate.category.equals("Appartement") || transaction.estate.category.equals("Maison")) &&
    transaction.nature == "Vente" &&
    transaction.estate.constructedArea > 0
}

/**
 * Loads real estate transactions from CSV files located at specified URLs for each year within a given range.
 *
 * This function constructs URLs for transaction data files based on a range of years obtained from `envVars`.
 * It then fetches and processes the data from each URL in parallel. The processing involves several steps:
 * - Decompressing the data if it's in GZIP format.
 * - Decoding the UTF-8 encoded data.
 * - Splitting the data into lines.
 * - Parsing each line as a CSV with a specified separator.
 * - Filtering out lines that don't correspond to valid transactions.
 *
 * If the data for a specific year is successfully fetched and processed, it is converted into a `ZStream` of
 * `Transaction` objects. If the data for a year is not available, an empty `ZStream` is returned for that year.
 *
 * @param envVars Contains environment variables including the start and end years for the range, the base URL for data, and the CSV separator.
 * @return A `ZIO` effect that, when executed, will either yield a `Map` from year to `ZStream` of `Transaction` objects
 *         or fail with a `Throwable` if an error occurs during data fetching or processing.
 *
 * Example:
 * {{{
 *   val envVars = EnvVars(...)
 *   val transactionsByYear: ZIO[Scope & Client, Throwable, Map[Int, ZStream[Any, Throwable, Transaction]]] = loadTransactions(envVars)
 *   transactionsByYear.map { transactions =>
 *     // Process transactions here
 *   }
 * }}}
 */
def loadTransactions(envVars: EnvVars): ZIO[Scope & Client, Throwable, Map[Int, ZStream[Any, Throwable, Transaction]]] = {
  val urlsByYear = (envVars.startYear to envVars.endYear).map(year => (year, s"${envVars.dataUrl}/$year/full.csv.gz"))

  ZIO.foreachPar(urlsByYear) { case (year, url) =>
    Console.printLine(s"> [$year] Fetching data from $url")

    fetchData(url).flatMap {
      case Some(dataStream) =>
        ZIO.succeed(
          year -> decompressGzippedData(dataStream)
            .via(ZPipeline.utf8Decode)
            .via(ZPipeline.splitLines)
            .flatMap(line => parseCsvLine(line, envVars.csvSeparator))
            .collectSome
            .filter(validateTransaction))
      case None => ZIO.succeed(year -> ZStream.empty)
    }
  }.map(_.toMap)
}

/**
 * Filters a collection of real estate transactions based on user-specified criteria.
 *
 * This function applies user-defined filters to a map of transaction streams, each keyed by year.
 * It first filters transactions based on the year, selecting only the transactions from the specified year.
 * If no year is specified in the filters, it defaults to transactions from the year 2018.
 *
 * After filtering by year, it applies additional filters based on:
 * - Minimum transaction amount
 * - Maximum transaction amount
 * - Property type (e.g., apartment, house)
 *
 * Transactions that do not meet the specified criteria are filtered out.
 *
 * @param transactionsByYear A map of transaction streams, each associated with a specific year.
 * @param filters            The `UserFilters` object containing the filter criteria.
 * @return A `ZStream` of `Transaction` objects that meet the specified filter criteria.
 *
 *         Example:
 * {{{
 *   val transactions: Map[Int, ZStream[Any, Throwable, Transaction]] = ...
 *   val filters = UserFilters(...)
 *   val filteredTransactions: ZStream[Any, Throwable, Transaction] = filterTransactions(transactions, filters)
 *   filteredTransactions.foreach { transaction =>
 *     // Process each filtered transaction
 *   }
 * }}}
 */
def filterTransactions(
                        transactionsByYear: Map[Int, ZStream[Any, Throwable, Transaction]],
                        filters: UserFilters
                      ): ZStream[Any, Throwable, Transaction] = {
  transactionsByYear.getOrElse(filters.year.getOrElse(2018), ZStream.empty)
    .filter { transaction =>
      filters.geographicFilter match {
        case Some(GeographicFilter.CityFilter(city)) => City.isSimilar(transaction.estate.location.city, city)
        case Some(GeographicFilter.DepartmentFilter(departmentCode)) => DepartmentCode.compareCodes(departmentCode, transaction.estate.location.departmentCode)
        case None => true
      }
    }

    .filter { transaction =>
      filters.minAmount.forall(transaction.amount >= _) &&
        filters.maxAmount.forall(transaction.amount <= _) &&
        filters.propertyType.forall(_ == Category.value(transaction.estate.category))
    }

}
