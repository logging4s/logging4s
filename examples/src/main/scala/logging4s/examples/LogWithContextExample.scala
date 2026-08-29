package logging4s.examples

import cats.effect.{ExitCode, IO, IOApp}

import scala.language.implicitConversions

import logging4s.core.{Logging, LoggingContext}
import logging4s.core.syntax.all.*

import logging4s.cats.CatsInstances.given
import logging4s.logback.LogbackInstances.given
import logging4s.json.circe.CirceInstances.given

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

      scoped = logging.withContextValues(johnShow.id.asLogValue("actor_id"))
      _     <- scoped.info("Acting on behalf of the user")

      reScoped = scoped.withContextValues("system".asLogValue("actor_id"))
      _       <- reScoped.info("The inner scope replaces actor_id, it is not suffixed")

      _ <- reScoped.info("A call-site value wins over the context", "override".asLogValue("actor_id"))

      _ <- logging.info("All users created", Seq(johnShow))
    yield ExitCode.Success
