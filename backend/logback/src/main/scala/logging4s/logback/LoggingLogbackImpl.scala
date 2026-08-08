package logging4s.logback

import org.slf4j.Logger

import logging4s.core.{Delay, Logging, LoggingContext, LoggableValue}
import logging4s.core.syntax.plain

class LoggingLogbackImpl[F[*]: Delay](logger: Logger, context: LoggingContext = LoggingContext(Seq.empty)) extends Logging[F]:

  override def withContext(moreContext: LoggingContext): Logging[F] = LoggingLogbackImpl(logger, context + moreContext)

  override def error(message: String): F[Unit] =
    Delay[F].delay(logger.error(message))

  override def error(message: String, error: Throwable): F[Unit] =
    Delay[F].delay {
      logger.error(s"$message: class=${error.getClass.getName}, message=${error.getMessage}", error)
    }

  override def error(message: String, values: LoggableValue*): F[Unit] =
    Delay[F].delay {
      val valuesWithContext = context.values ++ values

      logger.error(MarkerHelper.fromLoggable(valuesWithContext), s"$message: ${valuesWithContext.plain}")
    }

  override def error(message: String, error: Throwable, values: LoggableValue*): F[Unit] =
    Delay[F].delay {
      val valuesWithContext = context.values ++ values

      logger.error(
        MarkerHelper.fromLoggable(valuesWithContext),
        s"$message: class=${error.getClass.getName}, message=${error.getMessage}, ${valuesWithContext.plain}",
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
      val valuesWithContext = context.values ++ values

      logger.warn(MarkerHelper.fromLoggable(valuesWithContext), s"$message: ${valuesWithContext.plain}")
    }

  override def warn(message: String, error: Throwable, values: LoggableValue*): F[Unit] =
    Delay[F].delay {
      val valuesWithContext = context.values ++ values

      logger.warn(
        MarkerHelper.fromLoggable(valuesWithContext),
        s"$message: class=${error.getClass.getName}, message=${error.getMessage}, ${valuesWithContext.plain}",
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
      val valuesWithContext = context.values ++ values

      logger.info(MarkerHelper.fromLoggable(valuesWithContext), s"$message: ${valuesWithContext.plain}")
    }

  override def info(message: String, error: Throwable, values: LoggableValue*): F[Unit] =
    Delay[F].delay {
      val valuesWithContext = context.values ++ values

      logger.info(
        MarkerHelper.fromLoggable(valuesWithContext),
        s"$message: class=${error.getClass.getName}, message=${error.getMessage}, ${valuesWithContext.plain}",
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
      val valuesWithContext = context.values ++ values

      logger.debug(MarkerHelper.fromLoggable(valuesWithContext), s"$message: ${valuesWithContext.plain}")
    }

  override def debug(message: String, error: Throwable, values: LoggableValue*): F[Unit] =
    Delay[F].delay {
      val valuesWithContext = context.values ++ values

      logger.debug(
        MarkerHelper.fromLoggable(valuesWithContext),
        s"$message: class=${error.getClass.getName}, message=${error.getMessage}, ${valuesWithContext.plain}",
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
      val valuesWithContext = context.values ++ values

      logger.trace(MarkerHelper.fromLoggable(valuesWithContext), s"$message: ${valuesWithContext.plain}")
    }

  override def trace(message: String, error: Throwable, values: LoggableValue*): F[Unit] =
    Delay[F].delay {
      val valuesWithContext = context.values ++ values

      logger.trace(
        MarkerHelper.fromLoggable(valuesWithContext),
        s"$message: class=${error.getClass.getName}, message=${error.getMessage}, ${valuesWithContext.plain}",
        error,
      )
    }
