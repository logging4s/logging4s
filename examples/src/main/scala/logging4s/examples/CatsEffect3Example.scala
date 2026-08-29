package logging4s.examples

import cats.effect.{ExitCode, IO, IOApp}

import scala.language.implicitConversions

import logging4s.core.Logging
import logging4s.core.syntax.all.*

import logging4s.cats.CatsInstances.given
import logging4s.logback.LogbackInstances.given

object CatsEffect3Example extends IOApp:

  private def createUser(name: String, age: Int): IO[User] =
    for id <- IO.randomUUID
    yield User(id, name, age)

  override def run(args: List[String]): IO[ExitCode] =
    Logging.create[IO]("CatsEffectExampleLogging").flatMap { logging =>
      given Logging[IO] = logging

      for
        johnShow <- createUser("John Show", 22)
        _        <- logging.info("User created", johnShow)

        daenerys <- createUser("Daenerys Targaryen", 22)
        _        <- info"user created: $daenerys"

        _ <- logging.info("All users created", Seq(johnShow, daenerys))

        failure = new IllegalStateException("raven never arrived")
        _ <- error"delivery failed: $failure"
      yield ExitCode.Success
    }
