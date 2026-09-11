package logging4s.console

import logging4s.core.config.LoggableEncodingConfig
import logging4s.core.{Delay, Logging, LoggingContext, LoggingFactory}

trait ConsoleInstances:

  given ConsoleLoggingFactory: (ConsoleConfig, LoggableEncodingConfig) => LoggingFactory:
    def create[F[*]: Delay](name: String, context: LoggingContext): F[Logging[F]] =
      Delay[F].delay(LoggingConsoleImpl(name, context))

object ConsoleInstances extends ConsoleInstances
