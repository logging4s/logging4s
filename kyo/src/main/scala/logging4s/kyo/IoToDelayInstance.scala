package logging4s.kyo

import kyo.{Frame, IO, <}

import logging4s.core.Delay

type KIO[T] = T < IO

trait IoToDelayInstance:

  given Delay[KIO] =
    new Delay[KIO]:
      override def delay[A](a: => A): KIO[A] =
        IO(a)

object IoToDelayInstance extends IoToDelayInstance