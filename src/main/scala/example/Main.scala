package example

import example.models.*
import example.types.RealEstateTypes._
import example.types.LocationTypes._
import io.github.cdimascio.dotenv.Dotenv
import zio.*
import zio.Console.readLine
import zio.http.*
import zio.stream.ZPipeline.gunzip
import zio.stream.{ZPipeline, ZStream}

import java.io.IOException
import java.time.format.DateTimeFormatter
import java.time.{LocalDate, ZoneId}
import java.util.Date

object Main extends ZIOAppDefault {

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
  private def loadEnvVars(): ZIO[Any, RuntimeException, EnvVars] = {
    ZIO.attempt {
      val dotenv = Dotenv.load()
      val dataUrl = Option(dotenv.get("DATA_URL")).getOrElse(throw new RuntimeException("No DATA_URL given in .env"))
      val startYear = Option(dotenv.get("START_YEAR")).fold(throw new RuntimeException("No START_YEAR given in .env"))(_.toInt)
      val endYear = Option(dotenv.get("END_YEAR")).fold(throw new RuntimeException("No END_YEAR given in .env"))(_.toInt)
      val csvSeparator = Option(dotenv.get("CSV_SEPARATOR"))

      EnvVars(dataUrl, startYear, endYear, csvSeparator)
    }.mapError(e => new RuntimeException("Error loading environment variables", e))
  }

  /**
   * Fetches data from a given URL using an HTTP client within a ZIO environment.
   *
   * This method decodes the provided URL and makes an HTTP request using the ZIO `Client`. The response
   * is then processed based on its status. If the response status is `Status.Ok`, the body of the response
   * is returned as a stream. Otherwise, an error message is printed to the console, and `None` is returned.
   *
   * The method is designed to be safe in terms of exceptions and resource management. Any failure in the process,
   * including invalid URLs or HTTP errors, is captured within the ZIO effect.
   *
   * Note: This method is private and is intended for internal use within its enclosing class or object.
   *
   * @param url The URL to fetch data from. The URL is expected to be encoded and valid.
   * @return A ZIO effect which, when executed, will perform the HTTP request and process the response.
   *         The effect requires a ZIO `Scope` for resource management and a `Client` for making the HTTP request.
   *         The result is an `Option[ZStream[Any, Throwable, Byte]]`. If the HTTP request is successful and
   *         returns a `Status.Ok`, a `Some(ZStream)` containing the response body as a stream of bytes is returned.
   *         For any other status, or if the URL is invalid, `None` is returned.
   * @throws RuntimeException If the URL is invalid and cannot be decoded.
   * @see [[zio.ZIO]], [[zio.stream.ZStream]], [[zio.Scope]], [[zio.http.Client]], [[zio.http.Request]]
   */
  private def fetchData(url: String): ZIO[Scope & Client, Throwable, Option[ZStream[Any, Throwable, Byte]]] = {
    val decodedUrl = URL.decode(url).getOrElse(throw new RuntimeException("Invalid URL"))

    ZIO.service[Client].flatMap { client =>
      client.request(Request(url = decodedUrl)).flatMap { response =>
        response.status match {
          case Status.Ok => ZIO.succeed(Some(response.body.asStream))
          case _ =>
            Console.printLine(s"> Failed to fetch data from $url - Status: ${response.status}").as(None)
        }
      }
    }
  }

  /**
   * Decompresses a stream of gzipped data.
   *
   * This method takes a `ZStream` of bytes representing gzipped data and applies a decompression
   * transformation to it using the `gunzip` function. The result is a new `ZStream` instance
   * that emits the decompressed data. This is useful for processing gzipped data received from
   * various sources such as files, network streams, etc.
   *
   * The decompression is done using a specified buffer size, which can impact the performance and memory
   * utilization of the operation. A larger buffer size can potentially increase performance at the cost
   * of higher memory usage.
   *
   * Note: This method is private and intended for internal use within its enclosing class or object.
   *
   * @param gzipped A `ZStream[Any, Throwable, Byte]` representing the gzipped data to be decompressed.
   *                It is a stream of bytes where each byte represents a part of the gzipped data.
   * @return A `ZStream[Any, Throwable, Byte]` that emits the decompressed data. This stream represents
   *         the raw data obtained after decompressing the input gzipped data. The stream can be further
   *         processed, saved, or transmitted as required.
   * @throws Throwable If the decompression process fails, the stream will emit the exception.
   *                   This could be due to various reasons, such as corrupted input data or
   *                   insufficient memory for the buffer.
   * @see [[zio.stream.ZStream]], [[zio.stream.ZPipeline#gunzip]]
   */
  private def decompressGzippedData(gzipped: ZStream[Any, Throwable, Byte]): ZStream[Any, Throwable, Byte] = {
    val bufferSize: Int = 64 * 1024
    gzipped.via(gunzip(bufferSize))
  }

  private val zoneId: ZoneId = ZoneId.systemDefault()
  private val dateFormatter: DateTimeFormatter = DateTimeFormatter.ofPattern("yyyy-MM-dd")

  /**
   * Parses a single line of a CSV file and attempts to create a `Transaction` object from it.
   *
   * This method takes a CSV line as a string and an optional separator (defaulting to ","). It then
   * splits the line into fields, trims each field, and maps these fields to the properties of a
   * `Transaction` object. This includes parsing and formatting dates, numbers, and handling optional
   * fields appropriately.
   *
   * The method is designed to be robust against malformed data. In case of parsing errors or if the
   * data does not conform to the expected format, the method silently fails and returns `None` instead
   * of a `Transaction` object.
   *
   * Note: This method is private and is intended for internal use within its enclosing class or object.
   *
   * @param line      A `String` representing a single line from a CSV file.
   * @param separator An `Option[String]` representing the delimiter used in the CSV file. If not provided,
   *                  the default separator "," is used.
   * @return A `ZStream[Any, Nothing, Option[Transaction]]`. The stream emits a single element which is an
   *         `Option[Transaction]`. If the line is successfully parsed, `Some[Transaction]` is emitted,
   *         otherwise `None` is emitted in case of any parsing errors.
   * @throws Throwable Silently handles any throwable (e.g., parsing errors) by emitting `None`.
   *                   This behavior ensures that a single malformed line does not disrupt the
   *                   processing of an entire CSV file.
   * @see [[zio.stream.ZStream]], [[java.text.SimpleDateFormat]], [[example.models.Transaction]],
   *      [[example.models.RealEstate]], [[example.models.Location]]
   */
  private def parseCsvLine(line: String, separator: Option[String]): ZStream[Any, Nothing, Option[Transaction]] = {
    ZStream.succeed {
      try {
        val fields = line.split(separator.getOrElse(","))

        val localDate = LocalDate.parse(fields(1), dateFormatter)
        val date = Date.from(localDate.atStartOfDay(zoneId).toInstant)
        val nature = fields(3)
        val amount = fields(4).toDouble

        val location = for {
          postalCode <- PostalCode(fields(9))
          departmentCode <- DepartmentCode(fields(12))
          geoPoint <- GeoPoint(fields(38).toDouble, fields(39).toDouble)
        } yield Location(
          number = Option(fields(5)).filter(_.nonEmpty).map(_.toInt),
          suffix = Option(fields(6)),
          street = fields(7),
          city = fields(11),
          departmentCode = departmentCode,
          postalCode = postalCode,
          geoPoint = geoPoint
        )

        for {
          loc <- location
          category <- Category(fields(30))
          roomCount <- Option(fields(32)).filter(_.nonEmpty).flatMap(s => RoomCount(s.toInt))
          constArea <- Option(fields(31)).filter(_.nonEmpty).flatMap(s => ConstructedArea(s.toInt))
          landArea <- Option(fields(37)).filter(_.nonEmpty).flatMap(s => LandArea(s.toInt))
        } yield Transaction(
          date,
          nature,
          amount,
          RealEstate(
            category = category,
            rooms = roomCount,
            location = loc,
            constructedArea = constArea,
            landArea = landArea
          )
        )
      } catch {
        case e: Throwable => None
      }
    }
  }


  // TODO : Documentation
  // TODO : Add more filters
  private def validateTransaction(transaction: Transaction): Boolean = {
    return transaction.amount > 0 &&
      (transaction.estate.category.equals("Appartement") || transaction.estate.category.equals("Maison")) &&
      transaction.nature == "Vente" &&
      transaction.estate.constructedArea > 0
    //      transaction.estate.rooms.exists(_ >= 3) &&
    //      transaction.estate.constructedArea.exists(_ >= 60) &&
    //      transaction.estate.landArea.exists(_ >= 60)

  }

  // TODO : Documentation
  private def loadTransactions(envVars: EnvVars): ZIO[Scope & Client, Throwable, Map[Int, ZStream[Any, Throwable, Transaction]]] = {
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

  private def computeMetrics(transactionStream: ZStream[Any, Throwable, Transaction]): ZIO[Any, Throwable, Metric] = {
    ZIO.scoped {
      transactionStream.broadcast(8, 16).flatMap { streams =>
        for {
          avgPerM2Fiber <- streams(0).run(avgPerM2Sink).forkScoped
          distribFiber <- streams(1).run(realEstateCategoryDistributionSink).forkScoped
          avgFiber <- streams(2).run(avgAmountSink).forkScoped
          countFiber <- streams(3).runCount.forkScoped
          avgRoomFiber <- streams(4).run(avgRoomSink).forkScoped
          avgConstructedAreaFiber <- streams(5).run(avgConstructedAreaSink).forkScoped
          avgLandAreaFiber <- streams(6).run(avgLandAreaSink).forkScoped
          medianTransactionAmountFiber <- streams(7).run(medianAmountSink).forkScoped
          avgPerM2 <- avgPerM2Fiber.join
          distrib <- distribFiber.join
          avg <- avgFiber.join
          count <- countFiber.join
          avgRoom <- avgRoomFiber.join
          avgConstructedArea <- avgConstructedAreaFiber.join
          avgLandArea <- avgLandAreaFiber.join
          medianTransactionAmount <- medianTransactionAmountFiber.join
        } yield Metric(
          averagePrice = avg,
          averagePricePerSquareMeter = avgPerM2,
          averageRoomCount = avgRoom,
          averageConstructedArea = avgConstructedArea,
          averageLandArea = avgLandArea,
          medianTransactionAmount = medianTransactionAmount,
          transactionCount = count,
          housingNatureDistribution = distrib
        )
      }
    }
  }

  private def getUserFilters: ZIO[Any, IOException, UserFilters] = for {
    year <- readLine("Entrez l'année de la transaction (ou laissez vide) : ").map(_.toIntOption)
    minAmount <- readLine("Entrez le montant minimum (ou laissez vide) : ").map(_.toIntOption)
    maxAmount <- readLine("Entrez le montant maximun (ou laissez vide) : ").map(_.toIntOption)
    propertyType <- readLine("Entrez le type de bien (appartement/maison) (ou laissez vide) : ")
  } yield UserFilters(year, minAmount, maxAmount, if (propertyType.isEmpty) None else Some(propertyType))


  private def filterTransactions(
                                  transactionsByYear: Map[Int, ZStream[Any, Throwable, Transaction]],
                                  filters: UserFilters
                                ): ZStream[Any, Throwable, Transaction] = {

    val yearFilteredStream = filters.year match {
      case Some(yr) => transactionsByYear.getOrElse(yr, ZStream.empty)
      case None => transactionsByYear.getOrElse(2018, ZStream.empty)
    }

    // Step 2: Apply Other Filters
    yearFilteredStream.filter { transaction =>
      filters.minAmount.forall(transaction.amount >= _) &&
        filters.maxAmount.forall(transaction.amount <= _) &&
        filters.propertyType.forall(Category.value(transaction.estate.category) == _)
    }
  }


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

