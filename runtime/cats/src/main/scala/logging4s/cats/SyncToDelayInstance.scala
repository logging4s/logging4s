package logging4s.cats

import cats.effect.kernel.Sync

import logging4s.core.Delay

trait SyncToDelayInstance:

  given SyncDelay: [F[*]: Sync] => Delay[F]:
    override def delay[A](a: => A): F[A] = Sync[F].delay(a)
    override def unit: F[Unit]           = Sync[F].unit

object SyncToDelayInstance extends SyncToDelayInstance
