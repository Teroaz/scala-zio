package escalade

import escalade.helpers.*
import escalade.services.*
import zio.*
import zio.http.*

object Main extends ZIOAppDefault {

  /**
   * Execute the main program logic.
   *
   * This method loads environment variables, processes transactions by year, applies user-defined filters
   * to the transactions, computes metrics, and prints the resulting metrics to the console in a loop.
   * The program runs indefinitely.
   *
   * @return A ZIO effect representing the execution of the program.
   */
  override def run = {
    val program = for {
      _ <- Console.printLine(
        """
          |==================================================================
          | Welcome to the Real Estate Transaction Analyzer!
          | This application helps you analyze real estate transactions.
          |
          | You will be prompted to enter various filters like:
          | - Transaction year
          | - Minimum and maximum amount
          | - Property type
          | - Geographic location (city or department)
          |
          | The application then processes and presents the data
          | based on your inputs. Let's get started!
          |==================================================================
        """.stripMargin)
      envVars <- loadEnvVars()
      transactionsByYear <- loadTransactions(envVars)
      _ <- getUserFilters.flatMap { filters =>
        val filteredTransactions = filterTransactions(transactionsByYear, filters)
        for {
          metrics <- computeMetrics(filteredTransactions)
          _ <- Console.printLine(s"Metrics: $metrics")
        } yield ()
      }.forever
    } yield ()


    program.provide(Scope.default, Http.customClientLayer)
  }
}

