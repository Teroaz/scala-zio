package example.helpers

import example.EnvVars
import example.models.Transaction
import zio.http.Client
import zio.stream.ZStream
import zio.{Scope, ZIO}
import zio.Console.printLine
import zio.http.*
import io.github.cdimascio.dotenv.Dotenv

/**
 * Loads environment variables required for the application.
 *
 * This method reads the necessary configuration values from the environment using the Dotenv library.
 * It specifically looks for the 'DATA_URL', 'START_YEAR', 'END_YEAR', and 'CSV_SEPARATOR' variables.
 *
 * If any of the required variables ('DATA_URL', 'START_YEAR', 'END_YEAR') are not found in the environment,
 * the method fails with a `RuntimeException`.
 *
 * @return A ZIO effect that, when evaluated, will return an instance of `EnvVars` containing
 *         the loaded environment variables. The effect fails with `RuntimeException` if any required
 *         environment variable is missing.
 *
 *         Example usage:
 * {{{
 *   val envVarsEffect: ZIO[Any, RuntimeException, EnvVars] = loadEnvVars()
 *   envVarsEffect.flatMap(envVars => ... ) // Further processing with EnvVars
 * }}}
 *
 *         Note:
 *         - This method is intended to be used at the start of the application to load necessary configuration.
 *         - The method relies on the Dotenv library to access environment variables.
 * @throws RuntimeException if any required environment variable ('DATA_URL', 'START_YEAR', 'END_YEAR') is missing.
 * @see [[zio.ZIO]], [[io.github.cdimascio.dotenv.Dotenv]]
 */
def loadEnvVars(): ZIO[Any, RuntimeException, EnvVars] = {
  ZIO.attempt {
    val dotenv = Dotenv.load()
    val dataUrl = Option(dotenv.get("DATA_URL")).getOrElse(throw new RuntimeException("No DATA_URL given in .env"))
    val startYear = Option(dotenv.get("START_YEAR")).fold(throw new RuntimeException("No START_YEAR given in .env"))(_.toInt)
    val endYear = Option(dotenv.get("END_YEAR")).fold(throw new RuntimeException("No END_YEAR given in .env"))(_.toInt)
    val csvSeparator = Option(dotenv.get("CSV_SEPARATOR"))

    EnvVars(dataUrl, startYear, endYear, csvSeparator)
  }.mapError(e => new RuntimeException("Error loading environment variables", e))
}
