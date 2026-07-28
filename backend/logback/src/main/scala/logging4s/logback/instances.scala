package logging4s.logback

import com.typesafe.scalalogging.Logger

import logging4s.core.{Delay, Logging, LoggingFactory, LoggingContext}

trait instances:

  given LoggingFactory with
    def create[F[*]: Delay](name: String, context: LoggingContext): F[Logging[F]] =
      Delay[F].delay(LoggingLogbackImpl(Logger(name), context))

object instances extends instances
