package logging4s.examples

import java.util.UUID

import zio.{Task, ZIO, ZIOAppDefault}

import logging4s.core.Logging

import logging4s.zio.ZioInstances.given
import logging4s.logback.LogbackInstances.given

object ZioExample extends ZIOAppDefault:

  private def createUser(name: String, age: Int): Task[User] =
    for id <- ZIO.attempt(UUID.randomUUID())
    yield User(id, name, age)

  override def run: ZIO[Any, Any, Any] =
    for
      logging <- Logging.create[Task]("ZioExampleLogging")

      johnShow <- createUser("John Show", 22)
      _        <- logging.info("User created", johnShow)

      daenerys <- createUser("Daenerys Targaryen", 22)
      _        <- logging.info("User created", daenerys)

      _ <- logging.info("All users created", Seq.empty[User])
    yield ()
