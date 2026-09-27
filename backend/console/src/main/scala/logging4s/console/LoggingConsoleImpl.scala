package logging4s.console

import java.io.PrintStream

import logging4s.core.config.LoggableEncodingConfig
import logging4s.core.{Delay, Level, LogRecord, Logging, LoggableValue, LoggingContext, Position}

private[console] class LoggingConsoleImpl[F[*]: Delay](name: String, context: LoggingContext = LoggingContext.empty)(using
    console: ConsoleConfig,
    encoding: LoggableEncodingConfig,
) extends Logging[F]:

  private val ctxValues = LoggableValue.normalizeKeys(context.values)

  override def withContext(moreContext: LoggingContext): Logging[F] =
    LoggingConsoleImpl(name, LogRecord.context(ctxValues, moreContext))

  override def unit: F[Unit] = Delay[F].unit

  private val threshold = console.thresholdFor(name)

  override def enabled(level: Level): Boolean = threshold.allows(level)

  private def target: PrintStream =
    console.stream match
      case Stream.Stdout => System.out
      case Stream.Stderr => System.err

  override def emit(level: Level, message: String, cause: Option[Throwable], values: Seq[LoggableValue])(using position: Position): F[Unit] =
    if !enabled(level) then unit
    else
      Delay[F].delay {
        val record = LogRecord.prepare(ctxValues, values, position, LogRecord.ReservedKeys)
        target.println(Renderer.render(console, level, name, message, cause, record))
      }
