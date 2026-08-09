package logging4s.log4j2

import org.apache.logging.log4j.Logger

import logging4s.core.{Delay, Logging, LoggableValue, LoggingContext}
import logging4s.core.config.LoggableEncodingConfig
import logging4s.core.syntax.plain

class LoggingLog4j2Impl[F[*]: Delay](logger: Logger, context: LoggingContext = LoggingContext.empty)(using
    cfg: LoggableEncodingConfig
) extends Logging[F]:

  private val ctxValues = LoggableValue.normalizeKeys(context.values)

  override def withContext(moreContext: LoggingContext): Logging[F] = LoggingLog4j2Impl(logger, context + moreContext)

  private def buildMessage(message: String, values: Seq[LoggableValue]): LoggableMapMessage =
    val all          = ctxValues ++ LoggableValue.normalizeKeys(values)
    val deduplicated = LoggableValue.deduplicateKeys(all)
    val entries      = deduplicated.map(v => v.key.value -> v.json.value).toMap
    LoggableMapMessage(entries, s"$message: ${deduplicated.plain}")

  override def error(message: String): F[Unit] =
    Delay[F].delay(logger.error(message))

  override def error(message: String, error: Throwable): F[Unit] =
    Delay[F].delay(logger.error(s"$message: class=${error.getClass.getName}, message=${error.getMessage}", error))

  override def error(message: String, values: LoggableValue*): F[Unit] =
    Delay[F].delay {
      if logger.isErrorEnabled then logger.error(buildMessage(message, values))
    }

  override def error(message: String, error: Throwable, values: LoggableValue*): F[Unit] =
    Delay[F].delay {
      if logger.isErrorEnabled then
        val msg = buildMessage(s"$message: class=${error.getClass.getName}, message=${error.getMessage}", values)
        logger.error(msg, error)
    }

  override def warn(message: String): F[Unit] =
    Delay[F].delay(logger.warn(message))

  override def warn(message: String, error: Throwable): F[Unit] =
    Delay[F].delay(logger.warn(s"$message: class=${error.getClass.getName}, message=${error.getMessage}", error))

  override def warn(message: String, values: LoggableValue*): F[Unit] =
    Delay[F].delay {
      if logger.isWarnEnabled then logger.warn(buildMessage(message, values))
    }

  override def warn(message: String, error: Throwable, values: LoggableValue*): F[Unit] =
    Delay[F].delay {
      if logger.isWarnEnabled then
        val msg = buildMessage(s"$message: class=${error.getClass.getName}, message=${error.getMessage}", values)
        logger.warn(msg, error)
    }

  override def info(message: String): F[Unit] =
    Delay[F].delay(logger.info(message))

  override def info(message: String, error: Throwable): F[Unit] =
    Delay[F].delay(logger.info(s"$message: class=${error.getClass.getName}, message=${error.getMessage}", error))

  override def info(message: String, values: LoggableValue*): F[Unit] =
    Delay[F].delay {
      if logger.isInfoEnabled then logger.info(buildMessage(message, values))
    }

  override def info(message: String, error: Throwable, values: LoggableValue*): F[Unit] =
    Delay[F].delay {
      if logger.isInfoEnabled then
        val msg = buildMessage(s"$message: class=${error.getClass.getName}, message=${error.getMessage}", values)
        logger.info(msg, error)
    }

  override def debug(message: String): F[Unit] =
    Delay[F].delay(logger.debug(message))

  override def debug(message: String, error: Throwable): F[Unit] =
    Delay[F].delay(logger.debug(s"$message: class=${error.getClass.getName}, message=${error.getMessage}", error))

  override def debug(message: String, values: LoggableValue*): F[Unit] =
    Delay[F].delay {
      if logger.isDebugEnabled then logger.debug(buildMessage(message, values))
    }

  override def debug(message: String, error: Throwable, values: LoggableValue*): F[Unit] =
    Delay[F].delay {
      if logger.isDebugEnabled then
        val msg = buildMessage(s"$message: class=${error.getClass.getName}, message=${error.getMessage}", values)
        logger.debug(msg, error)
    }

  override def trace(message: String): F[Unit] =
    Delay[F].delay(logger.trace(message))

  override def trace(message: String, error: Throwable): F[Unit] =
    Delay[F].delay(logger.trace(s"$message: class=${error.getClass.getName}, message=${error.getMessage}", error))

  override def trace(message: String, values: LoggableValue*): F[Unit] =
    Delay[F].delay {
      if logger.isTraceEnabled then logger.trace(buildMessage(message, values))
    }

  override def trace(message: String, error: Throwable, values: LoggableValue*): F[Unit] =
    Delay[F].delay {
      if logger.isTraceEnabled then
        val msg = buildMessage(s"$message: class=${error.getClass.getName}, message=${error.getMessage}", values)
        logger.trace(msg, error)
    }
