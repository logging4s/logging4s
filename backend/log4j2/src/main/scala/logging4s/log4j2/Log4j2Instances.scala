package logging4s.log4j2

import org.apache.logging.log4j.LogManager

import logging4s.core.{Delay, Logging, LoggingFactory, LoggingContext}

trait Log4j2Instances:

  given LoggingFactory with
    def create[F[*]: Delay](name: String, context: LoggingContext): F[Logging[F]] =
      Delay[F].delay(LoggingLog4j2Impl(LogManager.getLogger(name), context))

object Log4j2Instances extends Log4j2Instances
