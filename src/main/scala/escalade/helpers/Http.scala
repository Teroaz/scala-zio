package escalade.helpers

import zio.Console.printLine
import zio.http.*
import zio.http.ZClient.{Config, live}
import zio.http.netty.NettyConfig
import zio.stream.ZStream
import zio.{Duration, Scope, ZIO, ZLayer}

object Http {
  /**
   * Fetches data from a URL using ZIO's HTTP client.
   *
   * Decodes the URL, makes an HTTP request, and returns the response body as a stream if the status is `Status.Ok`.
   *
   * @param url The encoded and valid URL to fetch data from.
   * @return A ZIO effect that performs the HTTP request and returns an `Option[ZStream]`.
   * @throws RuntimeException If the URL is invalid and cannot be decoded.
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


  private val customClientConfig: Config = Config.default.copy(
    idleTimeout = None,
  )

  val customClientLayer: ZLayer[Any, Throwable, Client] = {
    val clientConfigLayer = ZLayer.succeed(customClientConfig)
    val nettyConfig = ZLayer.succeed(NettyConfig.default.copy(shutdownTimeoutDuration = Duration.Infinity));

    (clientConfigLayer ++ DnsResolver.default ++ nettyConfig) >>> live
  }
}