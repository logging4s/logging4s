package logging4s.examples

import cats.effect.{ExitCode, IO, IOApp}

import logging4s.core.Logging
import logging4s.core.syntax.all.*

import logging4s.cats.CatsInstances.given
import logging4s.console.ConsoleInstances.given

object NamedTupleExample extends IOApp:

  override def run(args: List[String]): IO[ExitCode] =
    Logging.create[IO]("NamedTupleExample").flatMap { logging =>
      given Logging[IO] = logging

      val price = (currency = "EUR", cents = 1999)
      val route = (from = "AMS", to = "LIS", legs = (outbound = 1, inbound = 1))
      val wide  = (a = 1, b = 2, c = 3, d = 4, e = 5, f = 6, g = 7)

      for
        _ <- logging.info("order priced", price.asLogValue("price"))
        _ <- logging.info("route resolved", route.asLogValue("route"))
        _ <- logging.info("no arity limit, unlike plain tuples", wide.asLogValue("counters"))
        _ <- info"cart totalled $price"
      yield ExitCode.Success
    }
