package logging4s.core

trait Delay[F[*]]:
  def delay[A](a: A): F[A]

object Delay:
  def apply[F[*]](using F: Delay[F]): F.type = F
