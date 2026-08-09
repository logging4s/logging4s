package logging4s.logback

import org.slf4j.Logger

import logging4s.core.{Delay, Logging, LoggableValue, LoggingContext}
import logging4s.core.config.LoggableEncodingConfig
import logging4s.core.syntax.plain

class LoggingLogbackImpl[F[*]: Delay](logger: Logger, context: LoggingContext = LoggingContext.empty)(using
    cfg: LoggableEncodingConfig
) extends Logging[F]:

  private val ctxValues = LoggableValue.normalizeKeys(context.values)

  private def merge(values: Seq[LoggableValue]): Seq[LoggableValue] =
    ctxValues ++ LoggableValue.normalizeKeys(values)

  override def withContext(moreContext: LoggingContext): Logging[F] = LoggingLogbackImpl(logger, context + moreContext)

  override def error(message: String): F[Unit] =
    Delay[F].delay(logger.error(message))

  override def error(message: String, error: Throwable): F[Unit] =
    Delay[F].delay {
      logger.error(s"$message: class=${error.getClass.getName}, message=${error.getMessage}", error)
    }

  override def error(message: String, values: LoggableValue*): F[Unit] =
    Delay[F].delay {
      if logger.isErrorEnabled then
        val all = merge(values)
        logger.error(MarkerHelper.fromLoggable(all), s"$message: ${all.plain}")
    }

  override def error(message: String, error: Throwable, values: LoggableValue*): F[Unit] =
    Delay[F].delay {
      if logger.isErrorEnabled then
        val all = merge(values)
        logger.error(
          MarkerHelper.fromLoggable(all),
          s"$message: class=${error.getClass.getName}, message=${error.getMessage}, ${all.plain}",
          error,
        )
    }

  override def warn(message: String): F[Unit] =
    Delay[F].delay(logger.warn(message))

  override def warn(message: String, error: Throwable): F[Unit] =
    Delay[F].delay {
      logger.warn(s"$message: class=${error.getClass.getName}, message=${error.getMessage}", error)
    }

  override def warn(message: String, values: LoggableValue*): F[Unit] =
    Delay[F].delay {
      if logger.isWarnEnabled then
        val all = merge(values)
        logger.warn(MarkerHelper.fromLoggable(all), s"$message: ${all.plain}")
    }

  override def warn(message: String, error: Throwable, values: LoggableValue*): F[Unit] =
    Delay[F].delay {
      if logger.isWarnEnabled then
        val all = merge(values)
        logger.warn(
          MarkerHelper.fromLoggable(all),
          s"$message: class=${error.getClass.getName}, message=${error.getMessage}, ${all.plain}",
          error,
        )
    }

  override def info(message: String): F[Unit] =
    Delay[F].delay(logger.info(message))

  override def info(message: String, error: Throwable): F[Unit] =
    Delay[F].delay {
      logger.info(s"$message: class=${error.getClass.getName}, message=${error.getMessage}", error)
    }

  override def info(message: String, values: LoggableValue*): F[Unit] =
    Delay[F].delay {
      if logger.isInfoEnabled then
        val all = merge(values)
        logger.info(MarkerHelper.fromLoggable(all), s"$message: ${all.plain}")
    }

  override def info(message: String, error: Throwable, values: LoggableValue*): F[Unit] =
    Delay[F].delay {
      if logger.isInfoEnabled then
        val all = merge(values)
        logger.info(
          MarkerHelper.fromLoggable(all),
          s"$message: class=${error.getClass.getName}, message=${error.getMessage}, ${all.plain}",
          error,
        )
    }

  override def debug(message: String): F[Unit] =
    Delay[F].delay(logger.debug(message))

  override def debug(message: String, error: Throwable): F[Unit] =
    Delay[F].delay {
      logger.debug(s"$message: class=${error.getClass.getName}, message=${error.getMessage}", error)
    }

  override def debug(message: String, values: LoggableValue*): F[Unit] =
    Delay[F].delay {
      if logger.isDebugEnabled then
        val all = merge(values)
        logger.debug(MarkerHelper.fromLoggable(all), s"$message: ${all.plain}")
    }

  override def debug(message: String, error: Throwable, values: LoggableValue*): F[Unit] =
    Delay[F].delay {
      if logger.isDebugEnabled then
        val all = merge(values)
        logger.debug(
          MarkerHelper.fromLoggable(all),
          s"$message: class=${error.getClass.getName}, message=${error.getMessage}, ${all.plain}",
          error,
        )
    }

  override def trace(message: String): F[Unit] =
    Delay[F].delay(logger.trace(message))

  override def trace(message: String, error: Throwable): F[Unit] =
    Delay[F].delay {
      logger.trace(s"$message: class=${error.getClass.getName}, message=${error.getMessage}", error)
    }

  override def trace(message: String, values: LoggableValue*): F[Unit] =
    Delay[F].delay {
      if logger.isTraceEnabled then
        val all = merge(values)
        logger.trace(MarkerHelper.fromLoggable(all), s"$message: ${all.plain}")
    }

  override def trace(message: String, error: Throwable, values: LoggableValue*): F[Unit] =
    Delay[F].delay {
      if logger.isTraceEnabled then
        val all = merge(values)
        logger.trace(
          MarkerHelper.fromLoggable(all),
          s"$message: class=${error.getClass.getName}, message=${error.getMessage}, ${all.plain}",
          error,
        )
    }
