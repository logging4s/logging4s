package logging4s.log4j2

import org.apache.logging.log4j.Logger

import logging4s.core.{Delay, Logging, LoggingContext, LoggableValue}
import logging4s.core.syntax.plain

class LoggingLog4j2Impl[F[*]: Delay](logger: Logger, context: LoggingContext = LoggingContext.empty) extends Logging[F]:

  override def withContext(moreContext: LoggingContext): Logging[F] = LoggingLog4j2Impl(logger, context + moreContext)

  private def buildMessage(message: String, values: Seq[LoggableValue]): LoggableMapMessage =
    val deduplicated = LoggableValue.deduplicateKeys(values)
    val entries      = deduplicated.map(v => v.key.value -> v.json.value).toMap
    LoggableMapMessage(entries, s"$message: ${deduplicated.plain}")

  override def error(message: String): F[Unit] =
    Delay[F].delay(logger.error(message))

  override def error(message: String, error: Throwable): F[Unit] =
    Delay[F].delay(logger.error(s"$message: class=${error.getClass.getName}, message=${error.getMessage}", error))

  override def error(message: String, values: LoggableValue*): F[Unit] =
    Delay[F].delay {
      val valuesWithContext = context.values ++ values
      logger.error(buildMessage(message, valuesWithContext))
    }

  override def error(message: String, error: Throwable, values: LoggableValue*): F[Unit] =
    Delay[F].delay {
      val valuesWithContext = context.values ++ values
      val msg               = buildMessage(s"$message: class=${error.getClass.getName}, message=${error.getMessage}", valuesWithContext)
      logger.error(msg, error)
    }

  override def warn(message: String): F[Unit] =
    Delay[F].delay(logger.warn(message))

  override def warn(message: String, error: Throwable): F[Unit] =
    Delay[F].delay(logger.warn(s"$message: class=${error.getClass.getName}, message=${error.getMessage}", error))

  override def warn(message: String, values: LoggableValue*): F[Unit] =
    Delay[F].delay {
      val valuesWithContext = context.values ++ values
      logger.warn(buildMessage(message, valuesWithContext))
    }

  override def warn(message: String, error: Throwable, values: LoggableValue*): F[Unit] =
    Delay[F].delay {
      val valuesWithContext = context.values ++ values
      val msg               = buildMessage(s"$message: class=${error.getClass.getName}, message=${error.getMessage}", valuesWithContext)
      logger.warn(msg, error)
    }

  override def info(message: String): F[Unit] =
    Delay[F].delay(logger.info(message))

  override def info(message: String, error: Throwable): F[Unit] =
    Delay[F].delay(logger.info(s"$message: class=${error.getClass.getName}, message=${error.getMessage}", error))

  override def info(message: String, values: LoggableValue*): F[Unit] =
    Delay[F].delay {
      val valuesWithContext = context.values ++ values
      logger.info(buildMessage(message, valuesWithContext))
    }

  override def info(message: String, error: Throwable, values: LoggableValue*): F[Unit] =
    Delay[F].delay {
      val valuesWithContext = context.values ++ values
      val msg               = buildMessage(s"$message: class=${error.getClass.getName}, message=${error.getMessage}", valuesWithContext)
      logger.info(msg, error)
    }

  override def debug(message: String): F[Unit] =
    Delay[F].delay(logger.debug(message))

  override def debug(message: String, error: Throwable): F[Unit] =
    Delay[F].delay(logger.debug(s"$message: class=${error.getClass.getName}, message=${error.getMessage}", error))

  override def debug(message: String, values: LoggableValue*): F[Unit] =
    Delay[F].delay {
      val valuesWithContext = context.values ++ values
      logger.debug(buildMessage(message, valuesWithContext))
    }

  override def debug(message: String, error: Throwable, values: LoggableValue*): F[Unit] =
    Delay[F].delay {
      val valuesWithContext = context.values ++ values
      val msg               = buildMessage(s"$message: class=${error.getClass.getName}, message=${error.getMessage}", valuesWithContext)
      logger.debug(msg, error)
    }

  override def trace(message: String): F[Unit] =
    Delay[F].delay(logger.trace(message))

  override def trace(message: String, error: Throwable): F[Unit] =
    Delay[F].delay(logger.trace(s"$message: class=${error.getClass.getName}, message=${error.getMessage}", error))

  override def trace(message: String, values: LoggableValue*): F[Unit] =
    Delay[F].delay {
      val valuesWithContext = context.values ++ values
      logger.trace(buildMessage(message, valuesWithContext))
    }

  override def trace(message: String, error: Throwable, values: LoggableValue*): F[Unit] =
    Delay[F].delay {
      val valuesWithContext = context.values ++ values
      val msg               = buildMessage(s"$message: class=${error.getClass.getName}, message=${error.getMessage}", valuesWithContext)
      logger.trace(msg, error)
    }
