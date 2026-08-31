package logging4s.kyo

import kyo.Sync

import logging4s.core.Delay

trait SyncToDelayInstance:

  given KioDelay: Delay[KIO] = new:
    override def delay[A](a: => A): KIO[A] = Sync.defer(a)

    override val unit: KIO[Unit] = ()

object SyncToDelayInstance extends SyncToDelayInstance
