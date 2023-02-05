package logging4s.examples

import cats.effect.{ExitCode, IO, IOApp}
import logging4s.cats.Logging
import logging4s.core.LoggingContext

import logging4s.core.syntax.withKey

import logging4s.cats.instances.given
import logging4s.json.circe.instances.given

object LogWithContextExample extends IOApp:

  private def createUser(name: String, age: Int): IO[User] =
    for id <- IO.randomUUID
    yield User(id, name, age)

  override def run(args: List[String]): IO[ExitCode] =
    for
      context <- IO.randomUUID.map(uuid => LoggingContext(uuid.withKey("session_id")))
      logging <- Logging.create[IO]("CatsEffectExampleLogging", context)

      johnShow <- createUser("John Show", 22)
      _        <- logging.info("User created", johnShow)

      daenerys <- createUser("Daenerys Targaryen", 22)
      _        <- logging.info("User created", daenerys)

      _ <- logging.info("All users created", Seq(johnShow, daenerys))
    yield ExitCode.Success
