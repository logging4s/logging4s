package logging4s.cats

import cats.effect.Sync
import logging4s.core.Delay

trait SyncToDelayInstance:

  given [F[*]: Sync]: Delay[F] =
    new:
      override def delay[A](a: => A): F[A] = Sync[F].delay(a)

object SyncToDelayInstance extends SyncToDelayInstance
