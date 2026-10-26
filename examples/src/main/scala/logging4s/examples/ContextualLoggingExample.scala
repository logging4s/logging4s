package logging4s.examples

import cats.effect.{IO, IOApp}
import cats.syntax.parallel.*

import logging4s.core.syntax.all.*
import logging4s.cats.IOLocalContext
import logging4s.core.{LocalContext, Logging}

import logging4s.cats.CatsInstances.given
import logging4s.logback.LogbackInstances.given

object ContextualLoggingExample extends IOApp.Simple:

  private def handle(log: Logging[IO], order: Int): IO[Unit] =
    for
      _ <- log.info("order received", order.asLogValue("order_id"))
      _ <- log.info("order stored")
    yield ()

  private def request(local: LocalContext[IO], log: Logging[IO], id: String, orders: List[Int]): IO[Unit] =
    local.scoped(id.asLogValue("request_id")) {
      orders.parTraverse_(order => handle(log, order))
    }

  override def run: IO[Unit] =
    for
      local <- IOLocalContext.make
      base  <- Logging.create[IO]("ContextualLoggingExample")
      log    = Logging.contextual(base)(using local)
      _     <- (request(local, log, "r-1", List(1, 2)), request(local, log, "r-2", List(3))).parTupled
      _     <- log.info("all requests handled")
    yield ()
