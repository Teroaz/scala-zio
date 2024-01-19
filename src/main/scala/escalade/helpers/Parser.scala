package escalade.helpers

import escalade.models.{Location, RealEstate, Transaction}
import escalade.types.LocationTypes.{City, DepartmentCode, GeoPoint, PostalCode}
import escalade.types.RealEstateTypes.{Category, ConstructedArea, LandArea, RoomCount}
import zio.stream.ZPipeline.gunzip
import zio.stream.ZStream

import java.time.format.DateTimeFormatter
import java.time.{LocalDate, ZoneId}
import java.util.Date

private val zoneId: ZoneId = ZoneId.systemDefault()
private val dateFormatter: DateTimeFormatter = DateTimeFormatter.ofPattern("yyyy-MM-dd")

/**
 * Parses a CSV line into a `Transaction` object.
 *
 * This private method takes a CSV line and an optional separator, splits the line into fields,
 * and attempts to create a `Transaction` object from it. It handles parsing errors gracefully
 * and returns `None` in case of any issues.
 *
 * @param line      A CSV line as a `String`.
 * @param separator An optional delimiter (defaulting to ",").
 * @return A `ZStream` emitting an `Option[Transaction]`. Successful parsing results in `Some[Transaction]`,
 *         while parsing errors or invalid data result in `None`.
 * @throws Throwable Silently handles any errors during parsing.
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
 * Decompresses a gzipped data stream.
 *
 * Applies decompression to a `ZStream` of gzipped bytes using the `gunzip` function.
 * Returns a new `ZStream` emitting the decompressed data. Useful for handling gzipped
 * data from various sources.
 *
 * @param gzipped A `ZStream` of gzipped bytes to decompress.
 * @return A `ZStream` emitting the decompressed data.
 * @throws Throwable If decompression fails, the stream emits the exception.
 */
def decompressGzippedData(gzipped: ZStream[Any, Throwable, Byte]): ZStream[Any, Throwable, Byte] = {
  val bufferSize: Int = 64 * 1024
  gzipped.via(gunzip(bufferSize))
}

