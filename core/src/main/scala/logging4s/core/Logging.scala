package logging4s.core

import scala.util.Try
import scala.reflect.ClassTag

trait Logging[F[*]]:

  def withContext(context: LoggingContext): Logging[F]
  def withContextValues(values: LoggableValue*): Logging[F] = withContext(LoggingContext(values))

  def emit(level: Level, message: String, cause: Option[Throwable], values: Seq[LoggableValue]): F[Unit]

  def enabled(level: Level): Boolean

  def unit: F[Unit]

  final def error(message: String): F[Unit]                                           = emit(Level.Error, message, None, Nil)
  final def error(message: String, error: Throwable): F[Unit]                         = emit(Level.Error, message, Some(error), Nil)
  final def error(message: String, values: LoggableValue*): F[Unit]                   = emit(Level.Error, message, None, values)
  final def error(message: String, error: Throwable, values: LoggableValue*): F[Unit] = emit(Level.Error, message, Some(error), values)

  final def warn(message: String): F[Unit]                                           = emit(Level.Warn, message, None, Nil)
  final def warn(message: String, error: Throwable): F[Unit]                         = emit(Level.Warn, message, Some(error), Nil)
  final def warn(message: String, values: LoggableValue*): F[Unit]                   = emit(Level.Warn, message, None, values)
  final def warn(message: String, error: Throwable, values: LoggableValue*): F[Unit] = emit(Level.Warn, message, Some(error), values)

  final def info(message: String): F[Unit]                                           = emit(Level.Info, message, None, Nil)
  final def info(message: String, error: Throwable): F[Unit]                         = emit(Level.Info, message, Some(error), Nil)
  final def info(message: String, values: LoggableValue*): F[Unit]                   = emit(Level.Info, message, None, values)
  final def info(message: String, error: Throwable, values: LoggableValue*): F[Unit] = emit(Level.Info, message, Some(error), values)

  final def debug(message: String): F[Unit]                                           = emit(Level.Debug, message, None, Nil)
  final def debug(message: String, error: Throwable): F[Unit]                         = emit(Level.Debug, message, Some(error), Nil)
  final def debug(message: String, values: LoggableValue*): F[Unit]                   = emit(Level.Debug, message, None, values)
  final def debug(message: String, error: Throwable, values: LoggableValue*): F[Unit] = emit(Level.Debug, message, Some(error), values)

  final def trace(message: String): F[Unit]                                           = emit(Level.Trace, message, None, Nil)
  final def trace(message: String, error: Throwable): F[Unit]                         = emit(Level.Trace, message, Some(error), Nil)
  final def trace(message: String, values: LoggableValue*): F[Unit]                   = emit(Level.Trace, message, None, values)
  final def trace(message: String, error: Throwable, values: LoggableValue*): F[Unit] = emit(Level.Trace, message, Some(error), values)

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
