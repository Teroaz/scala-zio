package example

import zio.*
import zio.stream.*

object Main extends ZIOAppDefault {

  override def run: ZIO[Any & (ZIOAppArgs & Scope), Any, Unit] =
    for {
      _ <- ZStream.fromIterable((1 to 10000).toList)
        .take(10)
        .map(_ * 2)
        .foreach(Console.printLine(_))
    } yield ()
}
