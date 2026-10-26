package logging4s.core

import scala.annotation.targetName

trait LocalContext[F[*]]:
  def use[A](f: LoggingContext => F[A]): F[A]
  def scoped[A](context: LoggingContext)(fa: F[A]): F[A]

  @targetName("scopedValues")
  final def scoped[A](values: LoggableValue*)(fa: F[A]): F[A] = scoped(LoggingContext(values))(fa)

object LocalContext:
  def apply[F[*]](using local: LocalContext[F]): LocalContext[F] = local
