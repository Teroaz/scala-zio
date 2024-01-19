package example.helpers

import zio.http.{Client, Request, Status, URL}
import zio.stream.ZStream
import zio.{Scope, ZIO}
import zio.Console.printLine

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
def fetchData(url: String): ZIO[Scope & Client, Throwable, Option[ZStream[Any, Throwable, Byte]]] = {
  val decodedUrl = URL.decode(url).getOrElse(throw new RuntimeException("Invalid URL"))

  ZIO.service[Client].flatMap { client =>
    client.request(Request(url = decodedUrl)).flatMap { response =>
      response.status match {
        case Status.Ok => ZIO.succeed(Some(response.body.asStream))
        case _ =>
          printLine(s"> Failed to fetch data from $url - Status: ${response.status}").as(None)
      }
    }
  }
}
