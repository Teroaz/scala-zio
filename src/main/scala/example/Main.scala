package example

import zio.*
import zio.http.*
import example.helpers.*
import example.services.*

object Main extends ZIOAppDefault {
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

    program.provide(Client.default, Scope.default)
  }
}

