package logging4s.log4j2

import org.apache.logging.log4j.Logger

import logging4s.core.{Delay, Level, LogMessage, Logging, LoggableValue, LoggingContext}
import logging4s.core.config.LoggableEncodingConfig

private[log4j2] class LoggingLog4j2Impl[F[*]: Delay](logger: Logger, context: LoggingContext = LoggingContext.empty)(using
    cfg: LoggableEncodingConfig
) extends Logging[F]:

  private val ctxValues = LoggableValue.normalizeKeys(context.values)

  override def withContext(moreContext: LoggingContext): Logging[F] = LoggingLog4j2Impl(logger, context + moreContext)

  override def emit(level: Level, message: String, cause: Option[Throwable], values: Seq[LoggableValue]): F[Unit] =
    Delay[F].delay {
      if enabled(level) then
        val all          = ctxValues ++ LoggableValue.normalizeKeys(values)
        val deduplicated = LoggableValue.deduplicateKeys(all)
        val entries      = deduplicated.map(v => v.key.value -> v.json.value)
        val payload      = LoggableMapMessage(entries, LogMessage.render(message, cause, deduplicated))

        write(level, payload, cause.orNull)
    }

  override def unit: F[Unit] = Delay[F].unit

  override def enabled(level: Level): Boolean =
    level match
      case Level.Error => logger.isErrorEnabled
      case Level.Warn  => logger.isWarnEnabled
      case Level.Info  => logger.isInfoEnabled
      case Level.Debug => logger.isDebugEnabled
      case Level.Trace => logger.isTraceEnabled

  private def write(level: Level, message: LoggableMapMessage, cause: Throwable): Unit =
    level match
      case Level.Error => logger.error(message, cause)
      case Level.Warn  => logger.warn(message, cause)
      case Level.Info  => logger.info(message, cause)
      case Level.Debug => logger.debug(message, cause)
      case Level.Trace => logger.trace(message, cause)
