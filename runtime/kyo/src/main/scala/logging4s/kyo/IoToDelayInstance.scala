package logging4s.kyo

import kyo.{Frame, IO, <}

import logging4s.core.Delay

trait IoToDelayInstance:

  given KioDelay: Delay[KIO] = new:
    override def delay[A](a: => A): KIO[A] =
      IO(a)

    override val unit: KIO[Unit] = ()

object IoToDelayInstance extends IoToDelayInstance
