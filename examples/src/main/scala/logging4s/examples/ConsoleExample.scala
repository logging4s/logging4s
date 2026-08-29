package logging4s.examples

import cats.effect.{ExitCode, IO, IOApp}

import scala.language.implicitConversions

import logging4s.core.Logging
import logging4s.core.syntax.all.*

import logging4s.cats.CatsInstances.given
import logging4s.console.ConsoleInstances.given

object ConsoleExample extends IOApp:

  override def run(args: List[String]): IO[ExitCode] =
    Logging.create[IO]("ConsoleExample").flatMap { logging =>
      given Logging[IO] = logging

      for
        user <- IO.randomUUID.map(id => User(id, "John Snow", 22))
        _    <- logging.info("user created", user)

        requestId <- IO.randomUUID
        scoped     = logging.withContextValues(requestId.asLogValue("request_id"))
        _         <- scoped.warn("low balance", user)

        _ <- logging.error("payment failed", new RuntimeException("gateway timeout"))

        _ <- debug"verbose diagnostics for $user"
      yield ExitCode.Success
    }
