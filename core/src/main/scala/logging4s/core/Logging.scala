package logging4s.core

import scala.util.Try
import scala.reflect.ClassTag

import com.typesafe.scalalogging.Logger

trait Logging[F[*]]:

  def withContext(context: LoggingContext): Logging[F]

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

  def create[F[_]: Delay, S](using S: ClassTag[S]): F[Logging[F]] =
    Delay[F].delay(LoggingSlf4jImpl(Logger(S.runtimeClass)))

  def create[F[*]: Delay](name: String): F[Logging[F]] =
    Delay[F].delay(LoggingSlf4jImpl(Logger(name)))

  def create[F[*]: Delay, S](context: LoggingContext)(using S: ClassTag[S]): F[Logging[F]] =
    Delay[F].delay(LoggingSlf4jImpl(Logger(S.runtimeClass), context))

  def create[F[*]: Delay](name: String, context: LoggingContext): F[Logging[F]] =
    Delay[F].delay(LoggingSlf4jImpl(Logger(name), context))

  def createTry[S](using ClassTag[S]): Try[Logging[Try]] =
    create[Try, S]

  def createTry(name: String): Try[Logging[Try]] =
    create[Try](name)

  def createTry[S](context: LoggingContext)(using ClassTag[S]): Try[Logging[Try]] =
    create[Try, S](context)

  def createTry(name: String, context: LoggingContext): Try[Logging[Try]] =
    create[Try](name, context)

  def createEither[S](using ClassTag[S]): ThrowableEither[Logging[ThrowableEither]] =
    create[ThrowableEither, S]

  def createEither(name: String): ThrowableEither[Logging[ThrowableEither]] =
    create[ThrowableEither](name)

  def createEither[S](context: LoggingContext)(using ClassTag[S]): ThrowableEither[Logging[ThrowableEither]] =
    create[ThrowableEither, S](context)

  def createEither(name: String, context: LoggingContext): ThrowableEither[Logging[ThrowableEither]] =
    create[ThrowableEither](name, context)

  def createUnsafe[S](using ClassTag[S]): Identity[Logging[Identity]] =
    create[Identity, S]

  def createUnsafe(name: String): Identity[Logging[Identity]] =
    create[Identity](name)

  def createUnsafe[S](context: LoggingContext)(using ClassTag[S]): Identity[Logging[Identity]] =
    create[Identity, S](context)

  def createUnsafe(name: String, context: LoggingContext): Identity[Logging[Identity]] =
    create[Identity](name, context)
