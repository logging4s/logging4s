package logging4s.console

import java.io.PrintStream

import logging4s.core.{Delay, Logging, LoggableValue, LoggingContext}
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

  private def emit(level: Level, message: String, cause: Option[Throwable], values: Seq[LoggableValue]): F[Unit] =
    if !level.enabledAt(console.level) then Delay[F].delay(())
    else
      Delay[F].delay {
        val all = LoggableValue.deduplicateKeys(ctxValues ++ LoggableValue.normalizeKeys(values))
        target.println(Renderer.render(console, level, name, message, cause, all))
      }

  override def error(message: String): F[Unit]                                           = emit(Level.Error, message, None, Nil)
  override def error(message: String, error: Throwable): F[Unit]                         = emit(Level.Error, message, Some(error), Nil)
  override def error(message: String, values: LoggableValue*): F[Unit]                   = emit(Level.Error, message, None, values)
  override def error(message: String, error: Throwable, values: LoggableValue*): F[Unit] = emit(Level.Error, message, Some(error), values)

  override def warn(message: String): F[Unit]                                           = emit(Level.Warn, message, None, Nil)
  override def warn(message: String, error: Throwable): F[Unit]                         = emit(Level.Warn, message, Some(error), Nil)
  override def warn(message: String, values: LoggableValue*): F[Unit]                   = emit(Level.Warn, message, None, values)
  override def warn(message: String, error: Throwable, values: LoggableValue*): F[Unit] = emit(Level.Warn, message, Some(error), values)

  override def info(message: String): F[Unit]                                           = emit(Level.Info, message, None, Nil)
  override def info(message: String, error: Throwable): F[Unit]                         = emit(Level.Info, message, Some(error), Nil)
  override def info(message: String, values: LoggableValue*): F[Unit]                   = emit(Level.Info, message, None, values)
  override def info(message: String, error: Throwable, values: LoggableValue*): F[Unit] = emit(Level.Info, message, Some(error), values)

  override def debug(message: String): F[Unit]                                           = emit(Level.Debug, message, None, Nil)
  override def debug(message: String, error: Throwable): F[Unit]                         = emit(Level.Debug, message, Some(error), Nil)
  override def debug(message: String, values: LoggableValue*): F[Unit]                   = emit(Level.Debug, message, None, values)
  override def debug(message: String, error: Throwable, values: LoggableValue*): F[Unit] = emit(Level.Debug, message, Some(error), values)

  override def trace(message: String): F[Unit]                                           = emit(Level.Trace, message, None, Nil)
  override def trace(message: String, error: Throwable): F[Unit]                         = emit(Level.Trace, message, Some(error), Nil)
  override def trace(message: String, values: LoggableValue*): F[Unit]                   = emit(Level.Trace, message, None, values)
  override def trace(message: String, error: Throwable, values: LoggableValue*): F[Unit] = emit(Level.Trace, message, Some(error), values)
