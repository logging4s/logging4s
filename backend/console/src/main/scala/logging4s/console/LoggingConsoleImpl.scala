package logging4s.console

import java.io.PrintStream

import logging4s.core.{Delay, Level, Logging, LoggableValue, LoggingContext}
import logging4s.core.config.LoggableEncodingConfig

private[console] class LoggingConsoleImpl[F[*]: Delay](name: String, context: LoggingContext = LoggingContext.empty)(using
    console: ConsoleConfig,
    encoding: LoggableEncodingConfig,
) extends Logging[F]:

  private val ctxValues = LoggableValue.normalizeKeys(context.values)

  override def withContext(moreContext: LoggingContext): Logging[F] = LoggingConsoleImpl(name, context + moreContext)

  private def target: PrintStream =
    console.stream match
      case Stream.Stdout => System.out
      case Stream.Stderr => System.err

  override def emit(level: Level, message: String, cause: Option[Throwable], values: Seq[LoggableValue]): F[Unit] =
    if !level.enabledAt(console.level) then Delay[F].unit
    else
      Delay[F].delay {
        val all = LoggableValue.deduplicateKeys(ctxValues ++ LoggableValue.normalizeKeys(values))
        target.println(Renderer.render(console, level, name, message, cause, all))
      }
