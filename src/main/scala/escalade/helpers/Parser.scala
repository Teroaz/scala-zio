package escalade.helpers

import escalade.models.{Location, RealEstate, Transaction}
import escalade.types.LocationTypes.{City, DepartmentCode, GeoPoint, PostalCode}
import escalade.types.RealEstateTypes.{Category, ConstructedArea, LandArea, RoomCount}
import zio.stream.ZPipeline.gunzip
import zio.stream.ZStream

import java.time.{LocalDate, ZoneId}
import java.time.format.DateTimeFormatter
import java.util.Date

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
 * @see [[zio.stream.ZStream]], [[java.text.SimpleDateFormat]], [[escalade.models.Transaction]],
 *      [[escalade.models.RealEstate]], [[escalade.models.Location]]
 */
def parseCsvLine(line: String, separator: Option[String]): ZStream[Any, Nothing, Option[Transaction]] = {
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
        city <- City(fields(11))
      } yield Location(
        number = Option(fields(5)).filter(_.nonEmpty).map(_.toInt),
        suffix = Option(fields(6)),
        street = fields(7),
        city = city,
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
def decompressGzippedData(gzipped: ZStream[Any, Throwable, Byte]): ZStream[Any, Throwable, Byte] = {
  val bufferSize: Int = 64 * 1024
  gzipped.via(gunzip(bufferSize))
}
