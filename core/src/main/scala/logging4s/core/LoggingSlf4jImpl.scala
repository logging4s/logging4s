package logging4s.core

import com.typesafe.scalalogging.Logger

import LoggableValue.extensions.plain

class LoggingSlf4jImpl[F[*]: Delay as D](logger: Logger, context: LoggingContext = LoggingContext(Seq.empty)) extends Logging[F]:

  override def withContext(moreContext: LoggingContext): Logging[F] = LoggingSlf4jImpl(logger, context + moreContext)

  override def error(message: String): F[Unit] =
    D.delay(logger.error(message))

  override def error(message: String, error: Throwable): F[Unit] =
    D.delay {
      logger.error(
        message = s"$message: class=${error.getClass.getName}, message=${error.getMessage}",
        cause = error
      )
    }

  override def error(message: String, values: LoggableValue*): F[Unit] =
    D.delay {
      val valuesWithContext = context.values ++ values

      logger.error(
        marker = MarkerHelper.fromLoggable(valuesWithContext),
        message = s"$message: ${valuesWithContext.plain}"
      )
    }

  override def error(message: String, error: Throwable, values: LoggableValue*): F[Unit] =
    D.delay {
      val valuesWithContext = context.values ++ values

      logger.error(
        marker = MarkerHelper.fromLoggable(valuesWithContext),
        message = s"$message: class=${error.getClass.getName}, message=${error.getMessage}, ${valuesWithContext.plain}",
        cause = error
      )
    }

  override def warn(message: String): F[Unit] =
    D.delay(logger.warn(message))

  override def warn(message: String, error: Throwable): F[Unit] =
    D.delay {
      logger.warn(
        message = s"$message: class=${error.getClass.getName}, message=${error.getMessage}",
        cause = error
      )
    }

  override def warn(message: String, values: LoggableValue*): F[Unit] =
    D.delay {
      val valuesWithContext = context.values ++ values

      logger.warn(
        marker = MarkerHelper.fromLoggable(valuesWithContext),
        message = s"$message: ${valuesWithContext.plain}"
      )
    }

  override def warn(message: String, error: Throwable, values: LoggableValue*): F[Unit] =
    D.delay {
      val valuesWithContext = context.values ++ values

      logger.warn(
        marker = MarkerHelper.fromLoggable(valuesWithContext),
        message = s"$message: class=${error.getClass.getName}, message=${error.getMessage}, ${valuesWithContext.plain}",
        cause = error
      )
    }

  override def info(message: String): F[Unit] =
    D.delay(logger.info(message))

  override def info(message: String, error: Throwable): F[Unit] =
    D.delay {
      logger.info(
        message = s"$message: class=${error.getClass.getName}, message=${error.getMessage}",
        cause = error
      )
    }

  override def info(message: String, values: LoggableValue*): F[Unit] =
    D.delay {
      val valuesWithContext = context.values ++ values

      logger.info(
        marker = MarkerHelper.fromLoggable(valuesWithContext),
        message = s"$message: ${valuesWithContext.plain}"
      )
    }

  override def info(message: String, error: Throwable, values: LoggableValue*): F[Unit] =
    D.delay {
      val valuesWithContext = context.values ++ values

      logger.info(
        marker = MarkerHelper.fromLoggable(valuesWithContext),
        message = s"$message: class=${error.getClass.getName}, message=${error.getMessage}, ${valuesWithContext.plain}",
        cause = error
      )
    }

  override def debug(message: String): F[Unit] =
    D.delay(logger.debug(message))

  override def debug(message: String, error: Throwable): F[Unit] =
    D.delay {
      logger.debug(
        message = s"$message: class=${error.getClass.getName}, message=${error.getMessage}",
        cause = error
      )
    }

  override def debug(message: String, values: LoggableValue*): F[Unit] =
    D.delay {
      val valuesWithContext = context.values ++ values

      logger.debug(
        marker = MarkerHelper.fromLoggable(valuesWithContext),
        message = s"$message: ${valuesWithContext.plain}"
      )
    }

  override def debug(message: String, error: Throwable, values: LoggableValue*): F[Unit] =
    D.delay {
      val valuesWithContext = context.values ++ values

      logger.debug(
        marker = MarkerHelper.fromLoggable(valuesWithContext),
        message = s"$message: class=${error.getClass.getName}, message=${error.getMessage}, ${valuesWithContext.plain}",
        cause = error
      )
    }

  override def trace(message: String): F[Unit] =
    D.delay(logger.trace(message))

  override def trace(message: String, error: Throwable): F[Unit] =
    D.delay {
      logger.trace(
        message = s"$message: class=${error.getClass.getName}, message=${error.getMessage}",
        cause = error
      )
    }

  override def trace(message: String, values: LoggableValue*): F[Unit] =
    D.delay {
      val valuesWithContext = context.values ++ values

      logger.trace(
        marker = MarkerHelper.fromLoggable(valuesWithContext),
        message = s"$message: ${valuesWithContext.plain}"
      )
    }

  override def trace(message: String, error: Throwable, values: LoggableValue*): F[Unit] =
    D.delay {
      val valuesWithContext = context.values ++ values

      logger.trace(
        marker = MarkerHelper.fromLoggable(valuesWithContext),
        message = s"$message: class=${error.getClass.getName}, message=${error.getMessage}, ${valuesWithContext.plain}",
        cause = error
      )
    }
