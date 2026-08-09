package logging4s.logback

import org.slf4j.LoggerFactory

import logging4s.core.{Delay, Logging, LoggingFactory, LoggingContext}
import logging4s.core.config.LoggableEncodingConfig

trait LogbackInstances:

  given LogbackLoggingFactory(using LoggableEncodingConfig): LoggingFactory with
    def create[F[*]: Delay](name: String, context: LoggingContext): F[Logging[F]] =
      Delay[F].delay(LoggingLogbackImpl(LoggerFactory.getLogger(name), context))

object LogbackInstances extends LogbackInstances
