package logging4s.slf4j

import org.slf4j.LoggerFactory

import logging4s.core.{Delay, Logging, LoggingFactory, LoggingContext}

trait Slf4jInstances:

  given LoggingFactory with
    def create[F[*]: Delay](name: String, context: LoggingContext): F[Logging[F]] =
      Delay[F].delay(LoggingSlf4jImpl(LoggerFactory.getLogger(name), context))

object Slf4jInstances extends Slf4jInstances
