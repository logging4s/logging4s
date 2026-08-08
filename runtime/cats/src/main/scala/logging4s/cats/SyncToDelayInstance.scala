package logging4s.cats

import cats.effect.kernel.Sync

import logging4s.core.Delay

trait SyncToDelayInstance:

  given SyncDelay[F[*]: Sync]: Delay[F] with
    override def delay[A](a: => A): F[A] = Sync[F].delay(a)

object SyncToDelayInstance extends SyncToDelayInstance
