package logging4s.logback

import org.slf4j.Logger

import logging4s.core.config.LoggableEncodingConfig
import logging4s.core.{Delay, Level, LogMessage, LogRecord, Logging, LoggableValue, LoggingContext, Position}

private[logback] class LoggingLogbackImpl[F[*]: Delay](
    logger: Logger,
    context: LoggingContext = LoggingContext.empty,
)(using
    cfg: LoggableEncodingConfig
) extends Logging[F]:

  private val ctxValues = LoggableValue.normalizeKeys(context.values)

  override def withContext(moreContext: LoggingContext): Logging[F] =
    LoggingLogbackImpl(logger, LogRecord.context(ctxValues, moreContext))

  override def emit(level: Level, message: String, cause: Option[Throwable], values: Seq[LoggableValue])(using position: Position): F[Unit] =
    Delay[F].delay {
      if enabled(level) then
        val record = LogRecord.prepare(ctxValues, values, position, LogRecord.ReservedKeys)
        val text   = LogMessage.render(message, cause, record.values)

        if record.structured.isEmpty then write(level, text, cause)
        else write(level, MarkerHelper.fromLoggable(record.structured), text, cause)
    }

  override def unit: F[Unit] = Delay[F].unit

  override def enabled(level: Level): Boolean =
    level match
      case Level.Error => logger.isErrorEnabled
      case Level.Warn  => logger.isWarnEnabled
      case Level.Info  => logger.isInfoEnabled
      case Level.Debug => logger.isDebugEnabled
      case Level.Trace => logger.isTraceEnabled

  private def write(level: Level, message: String, cause: Option[Throwable]): Unit =
    level match
      case Level.Error => cause.fold(logger.error(message))(logger.error(message, _))
      case Level.Warn  => cause.fold(logger.warn(message))(logger.warn(message, _))
      case Level.Info  => cause.fold(logger.info(message))(logger.info(message, _))
      case Level.Debug => cause.fold(logger.debug(message))(logger.debug(message, _))
      case Level.Trace => cause.fold(logger.trace(message))(logger.trace(message, _))

  private def write(level: Level, marker: org.slf4j.Marker, message: String, cause: Option[Throwable]): Unit =
    level match
      case Level.Error => cause.fold(logger.error(marker, message))(logger.error(marker, message, _))
      case Level.Warn  => cause.fold(logger.warn(marker, message))(logger.warn(marker, message, _))
      case Level.Info  => cause.fold(logger.info(marker, message))(logger.info(marker, message, _))
      case Level.Debug => cause.fold(logger.debug(marker, message))(logger.debug(marker, message, _))
      case Level.Trace => cause.fold(logger.trace(marker, message))(logger.trace(marker, message, _))
