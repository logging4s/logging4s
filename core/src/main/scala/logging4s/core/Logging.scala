package logging4s.core

import scala.util.Try
import scala.reflect.ClassTag

trait Logging[F[*]]:

  def withContext(context: LoggingContext): Logging[F]
  def withContextValues(values: LoggableValue*): Logging[F] = withContext(LoggingContext(values))

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

  def create[F[*]: Delay, S](using factory: LoggingFactory, S: ClassTag[S]): F[Logging[F]] =
    factory.create(S.runtimeClass.getName, LoggingContext.empty)

  def create[F[*]: Delay](name: String)(using factory: LoggingFactory): F[Logging[F]] =
    factory.create(name, LoggingContext.empty)

  def create[F[*]: Delay, S](context: LoggingContext)(using factory: LoggingFactory, S: ClassTag[S]): F[Logging[F]] =
    factory.create(S.runtimeClass.getName, context)

  def create[F[*]: Delay](name: String, context: LoggingContext)(using factory: LoggingFactory): F[Logging[F]] =
    factory.create(name, context)

  def createTry[S](using factory: LoggingFactory, S: ClassTag[S]): Try[Logging[Try]] =
    create[Try, S]

  def createTry(name: String)(using factory: LoggingFactory): Try[Logging[Try]] =
    create[Try](name)

  def createTry[S](context: LoggingContext)(using factory: LoggingFactory, S: ClassTag[S]): Try[Logging[Try]] =
    create[Try, S](context)

  def createTry(name: String, context: LoggingContext)(using factory: LoggingFactory): Try[Logging[Try]] =
    create[Try](name, context)

  def createEither[S](using factory: LoggingFactory, S: ClassTag[S]): ThrowableEither[Logging[ThrowableEither]] =
    create[ThrowableEither, S]

  def createEither(name: String)(using factory: LoggingFactory): ThrowableEither[Logging[ThrowableEither]] =
    create[ThrowableEither](name)

  def createEither[S](
      context: LoggingContext
  )(using factory: LoggingFactory, S: ClassTag[S]): ThrowableEither[Logging[ThrowableEither]] =
    create[ThrowableEither, S](context)

  def createEither(name: String, context: LoggingContext)(using factory: LoggingFactory): ThrowableEither[Logging[ThrowableEither]] =
    create[ThrowableEither](name, context)

  def createUnsafe[S](using factory: LoggingFactory, S: ClassTag[S]): Identity[Logging[Identity]] =
    create[Identity, S]

  def createUnsafe(name: String)(using factory: LoggingFactory): Identity[Logging[Identity]] =
    create[Identity](name)

  def createUnsafe[S](context: LoggingContext)(using factory: LoggingFactory, S: ClassTag[S]): Identity[Logging[Identity]] =
    create[Identity, S](context)

  def createUnsafe(name: String, context: LoggingContext)(using factory: LoggingFactory): Identity[Logging[Identity]] =
    create[Identity](name, context)
