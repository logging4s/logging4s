package logging4s.slf4j

import org.slf4j.Logger
import org.slf4j.spi.LoggingEventBuilder

import logging4s.core.{Delay, Level, LogMessage, Logging, LoggableValue, LoggingContext}
import logging4s.core.config.LoggableEncodingConfig

private[slf4j] class LoggingSlf4jImpl[F[*]: Delay](logger: Logger, context: LoggingContext = LoggingContext.empty)(using
    cfg: LoggableEncodingConfig
) extends Logging[F]:

  private val ctxValues = LoggableValue.normalizeKeys(context.values)

  override def withContext(moreContext: LoggingContext): Logging[F] = LoggingSlf4jImpl(logger, context + moreContext)

  override def emit(level: Level, message: String, cause: Option[Throwable], values: Seq[LoggableValue]): F[Unit] =
    Delay[F].delay {
      if enabled(level) then
        val all          = ctxValues ++ LoggableValue.normalizeKeys(values)
        val deduplicated = LoggableValue.deduplicateKeys(all)

        val withCause     = cause.fold(builder(level))(builder(level).setCause)
        val withKeyValues = deduplicated.foldLeft(withCause) { (b, v) => b.addKeyValue(v.key.value, v.json.value) }

        withKeyValues.log(LogMessage.render(message, cause, deduplicated))
    }

  private def enabled(level: Level): Boolean =
    level match
      case Level.Error => logger.isErrorEnabled
      case Level.Warn  => logger.isWarnEnabled
      case Level.Info  => logger.isInfoEnabled
      case Level.Debug => logger.isDebugEnabled
      case Level.Trace => logger.isTraceEnabled

  private def builder(level: Level): LoggingEventBuilder =
    level match
      case Level.Error => logger.atError()
      case Level.Warn  => logger.atWarn()
      case Level.Info  => logger.atInfo()
      case Level.Debug => logger.atDebug()
      case Level.Trace => logger.atTrace()
