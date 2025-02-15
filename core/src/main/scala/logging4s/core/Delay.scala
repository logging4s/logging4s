package logging4s.core

import scala.util.Try

trait Delay[F[*]]:
  def delay[A](a: => A): F[A]

object Delay:
  def apply[F[*]](using F: Delay[F]): F.type = F

  given Delay[Identity]:
    override def delay[A](a: => A): Identity[A] = a

  given Delay[Try]:
    override def delay[A](a: => A): Try[A] = Try(a)

  given Delay[ThrowableEither]:
    override def delay[A](a: => A): ThrowableEither[A] =
      try
        a
        Right(a)
      catch case e: Throwable => Left(e)
