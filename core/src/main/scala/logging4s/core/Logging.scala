package logging4s.core

import scala.reflect.ClassTag

import com.typesafe.scalalogging.Logger

import LoggableValue.extensions.*

trait Logging[F[*]]:

  def error(message: String): F[Unit]
  def error(message: String, error: Throwable): F[Unit]
  def error(message: String, values: LoggableValue*): F[Unit]
  def error(message: String, error: Throwable, values: LoggableValue*): F[Unit]

  def warn(message: String): F[Unit]
  def warn(message: String, error: Throwable): F[Unit]
  def warn(message: String, values: LoggableValue*): F[Unit]
  def warn(message: String, error: Throwable, values: LoggableValue*): F[Unit]

  def info(message: String): F[Unit]
  def info(message: String, error: Throwable): F[Unit]
  def info(message: String, values: LoggableValue*): F[Unit]
  def info(message: String, error: Throwable, values: LoggableValue*): F[Unit]

  def debug(message: String): F[Unit]
  def debug(message: String, error: Throwable): F[Unit]
  def debug(message: String, values: LoggableValue*): F[Unit]
  def debug(message: String, error: Throwable, values: LoggableValue*): F[Unit]

  def trace(message: String): F[Unit]
  def trace(message: String, error: Throwable): F[Unit]
  def trace(message: String, values: LoggableValue*): F[Unit]
  def trace(message: String, error: Throwable, values: LoggableValue*): F[Unit]

object Logging:

  def apply[F[*]](using instance: Logging[F]): Logging[F] = instance

  def create[F[_]: Delay, S](using s: ClassTag[S]): F[Logging[F]] =
    Delay[F].delay(LoggingViaSlf4j(Logger(s.runtimeClass)))

  def create[F[*]: Delay](name: String): F[Logging[F]] =
    Delay[F].delay(LoggingViaSlf4j(Logger(name)))

  private final class LoggingViaSlf4j[F[*]: Delay](logger: Logger) extends Logging[F]:

    override def error(message: String): F[Unit] =
      Delay[F].delay(logger.error(message))

    override def error(message: String, error: Throwable): F[Unit] =
      Delay[F].delay {
        logger.error(
          message = s"$message: class=${error.getClass.getName}, message=${error.getMessage}",
          cause = error
        )
      }

    override def error(message: String, values: LoggableValue*): F[Unit] =
      Delay[F].delay {
        logger.error(
          marker = MarkerHelper.fromLoggable(values),
          message = s"$message: ${values.plain}"
        )
      }

    override def error(message: String, error: Throwable, values: LoggableValue*): F[Unit] =
      Delay[F].delay {
        logger.error(
          marker = MarkerHelper.fromLoggable(values),
          message = s"$message: class=${error.getClass.getName}, message=${error.getMessage}, ${values.plain}",
          cause = error
        )
      }

    override def warn(message: String): F[Unit] =
      Delay[F].delay(logger.warn(message))

    override def warn(message: String, error: Throwable): F[Unit] =
      Delay[F].delay {
        logger.warn(
          message = s"$message: class=${error.getClass.getName}, message=${error.getMessage}",
          cause = error
        )
      }

    override def warn(message: String, values: LoggableValue*): F[Unit] =
      Delay[F].delay {
        logger.warn(
          marker = MarkerHelper.fromLoggable(values),
          message = s"$message: ${values.plain}"
        )
      }

    override def warn(message: String, error: Throwable, values: LoggableValue*): F[Unit] =
      Delay[F].delay {
        logger.warn(
          marker = MarkerHelper.fromLoggable(values),
          message = s"$message: class=${error.getClass.getName}, message=${error.getMessage}, ${values.plain}",
          cause = error
        )
      }

    override def info(message: String): F[Unit] =
      Delay[F].delay(logger.info(message))

    override def info(message: String, error: Throwable): F[Unit] =
      Delay[F].delay {
        logger.info(
          message = s"$message: class=${error.getClass.getName}, message=${error.getMessage}",
          cause = error
        )
      }

    override def info(message: String, values: LoggableValue*): F[Unit] =
      Delay[F].delay {
        logger.info(
          marker = MarkerHelper.fromLoggable(values),
          message = s"$message: ${values.plain}"
        )
      }

    override def info(message: String, error: Throwable, values: LoggableValue*): F[Unit] =
      Delay[F].delay {
        logger.info(
          marker = MarkerHelper.fromLoggable(values),
          message = s"$message: class=${error.getClass.getName}, message=${error.getMessage}, ${values.plain}",
          cause = error
        )
      }

    override def debug(message: String): F[Unit] =
      Delay[F].delay(logger.debug(message))

    override def debug(message: String, error: Throwable): F[Unit] =
      Delay[F].delay {
        logger.debug(
          message = s"$message: class=${error.getClass.getName}, message=${error.getMessage}",
          cause = error
        )
      }

    override def debug(message: String, values: LoggableValue*): F[Unit] =
      Delay[F].delay {
        logger.debug(
          marker = MarkerHelper.fromLoggable(values),
          message = s"$message: ${values.plain}"
        )
      }

    override def debug(message: String, error: Throwable, values: LoggableValue*): F[Unit] =
      Delay[F].delay {
        logger.debug(
          marker = MarkerHelper.fromLoggable(values),
          message = s"$message: class=${error.getClass.getName}, message=${error.getMessage}, ${values.plain}",
          cause = error
        )
      }

    override def trace(message: String): F[Unit] =
      Delay[F].delay(logger.trace(message))

    override def trace(message: String, error: Throwable): F[Unit] =
      Delay[F].delay {
        logger.trace(
          message = s"$message: class=${error.getClass.getName}, message=${error.getMessage}",
          cause = error
        )
      }

    override def trace(message: String, values: LoggableValue*): F[Unit] =
      Delay[F].delay {
        logger.trace(
          marker = MarkerHelper.fromLoggable(values),
          message = s"$message: ${values.plain}"
        )
      }

    override def trace(message: String, error: Throwable, values: LoggableValue*): F[Unit] =
      Delay[F].delay {
        logger.trace(
          marker = MarkerHelper.fromLoggable(values),
          message = s"$message: class=${error.getClass.getName}, message=${error.getMessage}, ${values.plain}",
          cause = error
        )
      }
