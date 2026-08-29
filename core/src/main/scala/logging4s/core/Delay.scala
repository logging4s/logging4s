package logging4s.core

import scala.util.{Success, Try}
import scala.util.control.NonFatal

trait Delay[F[*]]:
  def delay[A](a: => A): F[A]
  def unit: F[Unit] = delay(())

object Delay:
  def apply[F[*]](using F: Delay[F]): F.type = F

  given Delay[Identity] = new:
    override def delay[A](a: => A): Identity[A] = a
    override val unit: Identity[Unit]           = ()

  given Delay[Try] = new:
    override def delay[A](a: => A): Try[A] = Try(a)
    override val unit: Try[Unit]           = Success(())

  given Delay[ThrowableEither] = new:
    override def delay[A](a: => A): ThrowableEither[A] =
      try Right(a)
      catch case NonFatal(e) => Left(e)

    override val unit: ThrowableEither[Unit] = Right(())
