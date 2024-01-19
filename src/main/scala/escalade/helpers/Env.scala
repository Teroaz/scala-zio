package escalade.helpers

import escalade.EnvVars
import io.github.cdimascio.dotenv.Dotenv
import zio.ZIO

/**
 * Loads environment variables required for the application from a .env file using Dotenv.
 *
 * @return A ZIO effect that returns an instance of `EnvVars` with loaded variables.
 * @throws RuntimeException if any required variable is missing.
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
