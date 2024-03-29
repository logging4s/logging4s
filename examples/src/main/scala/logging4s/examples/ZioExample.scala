package logging4s.examples

import zio.{Task, ZIO, ZIOAppDefault}
import logging4s.zio.LoggingZio

import java.util.UUID

object ZioExample extends ZIOAppDefault:

  private def createUser(name: String, age: Int): Task[User] =
    for id <- ZIO.attempt(UUID.randomUUID())
    yield User(id, name, age)

  override def run: ZIO[Any, Any, Any] =
    for
      logging <- LoggingZio.create("ZioExampleLogging")

      johnShow <- createUser("John Show", 22)
      _        <- logging.info("User created", johnShow)

      daenerys <- createUser("Daenerys Targaryen", 22)
      _        <- logging.info("User created", daenerys)

      _ <- logging.info("All users created", Seq.empty[User])
    yield ()
