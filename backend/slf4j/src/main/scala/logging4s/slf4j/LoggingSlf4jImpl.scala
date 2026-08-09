package logging4s.slf4j

import org.slf4j.Logger

import logging4s.core.{Delay, Logging, LoggableValue, LoggingContext}
import logging4s.core.config.LoggableEncodingConfig
import logging4s.core.syntax.plain

class LoggingSlf4jImpl[F[*]: Delay](logger: Logger, context: LoggingContext = LoggingContext.empty)(using
    cfg: LoggableEncodingConfig
) extends Logging[F]:

  // Context is stable for the logger's lifetime, so its keys are normalized once here instead of on every call.
  private val ctxValues = LoggableValue.normalizeKeys(context.values)

  private def merge(values: Seq[LoggableValue]): Seq[LoggableValue] =
    ctxValues ++ LoggableValue.normalizeKeys(values)

  override def withContext(moreContext: LoggingContext): Logging[F] = LoggingSlf4jImpl(logger, context + moreContext)

  private def logWithValues(
      values: Seq[LoggableValue],
      cause: Option[Throwable],
      message: Seq[LoggableValue] => String,
      builder: Logger => org.slf4j.spi.LoggingEventBuilder,
  ): Unit =
    val deduplicated  = LoggableValue.deduplicateKeys(values)
    val base          = builder(logger)
    val withCause     = cause.fold(base)(base.setCause)
    val withKeyValues = deduplicated.foldLeft(withCause) { (b, v) => b.addKeyValue(v.key.value, v.json.value) }

    withKeyValues.log(message(deduplicated))

  override def error(message: String): F[Unit] =
    Delay[F].delay(logger.error(message))

  override def error(message: String, error: Throwable): F[Unit] =
    Delay[F].delay(logger.error(s"$message: class=${error.getClass.getName}, message=${error.getMessage}", error))

  override def error(message: String, values: LoggableValue*): F[Unit] =
    Delay[F].delay {
      if logger.isErrorEnabled then logWithValues(merge(values), None, vs => s"$message: ${vs.plain}", _.atError())
    }

  override def error(message: String, error: Throwable, values: LoggableValue*): F[Unit] =
    Delay[F].delay {
      if logger.isErrorEnabled then
        logWithValues(
          merge(values),
          Some(error),
          vs => s"$message: class=${error.getClass.getName}, message=${error.getMessage}, ${vs.plain}",
          _.atError(),
        )
    }

  override def warn(message: String): F[Unit] =
    Delay[F].delay(logger.warn(message))

  override def warn(message: String, error: Throwable): F[Unit] =
    Delay[F].delay(logger.warn(s"$message: class=${error.getClass.getName}, message=${error.getMessage}", error))

  override def warn(message: String, values: LoggableValue*): F[Unit] =
    Delay[F].delay {
      if logger.isWarnEnabled then logWithValues(merge(values), None, vs => s"$message: ${vs.plain}", _.atWarn())
    }

  override def warn(message: String, error: Throwable, values: LoggableValue*): F[Unit] =
    Delay[F].delay {
      if logger.isWarnEnabled then
        logWithValues(
          merge(values),
          Some(error),
          vs => s"$message: class=${error.getClass.getName}, message=${error.getMessage}, ${vs.plain}",
          _.atWarn(),
        )
    }

  override def info(message: String): F[Unit] =
    Delay[F].delay(logger.info(message))

  override def info(message: String, error: Throwable): F[Unit] =
    Delay[F].delay(logger.info(s"$message: class=${error.getClass.getName}, message=${error.getMessage}", error))

  override def info(message: String, values: LoggableValue*): F[Unit] =
    Delay[F].delay {
      if logger.isInfoEnabled then logWithValues(merge(values), None, vs => s"$message: ${vs.plain}", _.atInfo())
    }

  override def info(message: String, error: Throwable, values: LoggableValue*): F[Unit] =
    Delay[F].delay {
      if logger.isInfoEnabled then
        logWithValues(
          merge(values),
          Some(error),
          vs => s"$message: class=${error.getClass.getName}, message=${error.getMessage}, ${vs.plain}",
          _.atInfo(),
        )
    }

  override def debug(message: String): F[Unit] =
    Delay[F].delay(logger.debug(message))

  override def debug(message: String, error: Throwable): F[Unit] =
    Delay[F].delay(logger.debug(s"$message: class=${error.getClass.getName}, message=${error.getMessage}", error))

  override def debug(message: String, values: LoggableValue*): F[Unit] =
    Delay[F].delay {
      if logger.isDebugEnabled then logWithValues(merge(values), None, vs => s"$message: ${vs.plain}", _.atDebug())
    }

  override def debug(message: String, error: Throwable, values: LoggableValue*): F[Unit] =
    Delay[F].delay {
      if logger.isDebugEnabled then
        logWithValues(
          merge(values),
          Some(error),
          vs => s"$message: class=${error.getClass.getName}, message=${error.getMessage}, ${vs.plain}",
          _.atDebug(),
        )
    }

  override def trace(message: String): F[Unit] =
    Delay[F].delay(logger.trace(message))

  override def trace(message: String, error: Throwable): F[Unit] =
    Delay[F].delay(logger.trace(s"$message: class=${error.getClass.getName}, message=${error.getMessage}", error))

  override def trace(message: String, values: LoggableValue*): F[Unit] =
    Delay[F].delay {
      if logger.isTraceEnabled then logWithValues(merge(values), None, vs => s"$message: ${vs.plain}", _.atTrace())
    }

  override def trace(message: String, error: Throwable, values: LoggableValue*): F[Unit] =
    Delay[F].delay {
      if logger.isTraceEnabled then
        logWithValues(
          merge(values),
          Some(error),
          vs => s"$message: class=${error.getClass.getName}, message=${error.getMessage}, ${vs.plain}",
          _.atTrace(),
        )
    }
