package logging4s.cats

import cats.effect.kernel.Sync

import logging4s.core.Delay

trait SyncToDelayInstance:

  given [F[*]: Sync as S] => Delay[F] =
    new Delay[F]:
      override def delay[A](a: => A): F[A] = S.delay(a)

object SyncToDelayInstance extends SyncToDelayInstance
