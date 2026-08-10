package logging4s.console

import logging4s.core.{Delay, Logging, LoggingContext, LoggingFactory}
import logging4s.core.config.LoggableEncodingConfig

trait ConsoleInstances:

  given ConsoleLoggingFactory(using ConsoleConfig, LoggableEncodingConfig): LoggingFactory with
    def create[F[*]: Delay](name: String, context: LoggingContext): F[Logging[F]] =
      Delay[F].delay(LoggingConsoleImpl(name, context))

object ConsoleInstances extends ConsoleInstances
