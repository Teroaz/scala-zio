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

