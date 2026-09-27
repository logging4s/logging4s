package logging4s.core

import scala.util.Try
import scala.reflect.ClassTag

trait Logging[F[*]]:
  self: Logging[F] =>

  def withContext(context: LoggingContext): Logging[F]
  def withContextValues(values: LoggableValue*): Logging[F] = withContext(LoggingContext(values))

  def emit(level: Level, message: String, cause: Option[Throwable], values: Seq[LoggableValue])(using Position): F[Unit]

  def enabled(level: Level): Boolean

  def unit: F[Unit]

  final def mapK[G[*]](f: [A] => F[A] => G[A]): Logging[G] =
    new:
      override def withContext(context: LoggingContext): Logging[G] = self.withContext(context).mapK(f)
      override def enabled(level: Level): Boolean                   = self.enabled(level)
      override def unit: G[Unit]                                    = f(self.unit)

      override def emit(level: Level, message: String, cause: Option[Throwable], values: Seq[LoggableValue])(using Position): G[Unit] =
        f(self.emit(level, message, cause, values))

  final inline def error(inline message: String)(using Position): F[Unit] =
    emit(Level.Error, LogMessage.static(message), None, Nil)

  final inline def error(inline message: String, error: Throwable)(using Position): F[Unit] =
    emit(Level.Error, LogMessage.static(message), Some(error), Nil)

  final inline def error(inline message: String, values: LoggableValue*)(using Position): F[Unit] =
    emit(Level.Error, LogMessage.static(message), None, values)

  final inline def error(inline message: String, error: Throwable, values: LoggableValue*)(using Position): F[Unit] =
    emit(Level.Error, LogMessage.static(message), Some(error), values)

  final inline def warn(inline message: String)(using Position): F[Unit] =
    emit(Level.Warn, LogMessage.static(message), None, Nil)

  final inline def warn(inline message: String, error: Throwable)(using Position): F[Unit] =
    emit(Level.Warn, LogMessage.static(message), Some(error), Nil)

  final inline def warn(inline message: String, values: LoggableValue*)(using Position): F[Unit] =
    emit(Level.Warn, LogMessage.static(message), None, values)

  final inline def warn(inline message: String, error: Throwable, values: LoggableValue*)(using Position): F[Unit] =
    emit(Level.Warn, LogMessage.static(message), Some(error), values)

  final inline def info(inline message: String)(using Position): F[Unit] =
    emit(Level.Info, LogMessage.static(message), None, Nil)

  final inline def info(inline message: String, error: Throwable)(using Position): F[Unit] =
    emit(Level.Info, LogMessage.static(message), Some(error), Nil)

  final inline def info(inline message: String, values: LoggableValue*)(using Position): F[Unit] =
    emit(Level.Info, LogMessage.static(message), None, values)

  final inline def info(inline message: String, error: Throwable, values: LoggableValue*)(using Position): F[Unit] =
    emit(Level.Info, LogMessage.static(message), Some(error), values)

  final inline def debug(inline message: String)(using Position): F[Unit] =
    emit(Level.Debug, LogMessage.static(message), None, Nil)

  final inline def debug(inline message: String, error: Throwable)(using Position): F[Unit] =
    emit(Level.Debug, LogMessage.static(message), Some(error), Nil)

  final inline def debug(inline message: String, values: LoggableValue*)(using Position): F[Unit] =
    emit(Level.Debug, LogMessage.static(message), None, values)

  final inline def debug(inline message: String, error: Throwable, values: LoggableValue*)(using Position): F[Unit] =
    emit(Level.Debug, LogMessage.static(message), Some(error), values)

  final inline def trace(inline message: String)(using Position): F[Unit] =
    emit(Level.Trace, LogMessage.static(message), None, Nil)

  final inline def trace(inline message: String, error: Throwable)(using Position): F[Unit] =
    emit(Level.Trace, LogMessage.static(message), Some(error), Nil)

  final inline def trace(inline message: String, values: LoggableValue*)(using Position): F[Unit] =
    emit(Level.Trace, LogMessage.static(message), None, values)

  final inline def trace(inline message: String, error: Throwable, values: LoggableValue*)(using Position): F[Unit] =
    emit(Level.Trace, LogMessage.static(message), Some(error), values)

object Logging:

  def apply[F[*]](using instance: Logging[F]): Logging[F] = instance

  def noop[F[*]: Delay]: Logging[F] =
    new:
      override val unit: F[Unit]                                    = Delay[F].unit
      override def withContext(context: LoggingContext): Logging[F] = this
      override def enabled(level: Level): Boolean                   = false

      override def emit(level: Level, message: String, cause: Option[Throwable], values: Seq[LoggableValue])(using Position): F[Unit] =
        unit

  def create[F[*]: Delay, S](using factory: LoggingFactory, S: ClassTag[S]): F[Logging[F]] =
    factory.create(S.runtimeClass.getName.stripSuffix("$"), LoggingContext.empty)

  def create[F[*]: Delay](name: String)(using factory: LoggingFactory): F[Logging[F]] =
    factory.create(name, LoggingContext.empty)

  def create[F[*]: Delay, S](context: LoggingContext)(using factory: LoggingFactory, S: ClassTag[S]): F[Logging[F]] =
    factory.create(S.runtimeClass.getName.stripSuffix("$"), context)

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
