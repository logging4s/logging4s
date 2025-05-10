package logging4s.zio

import zio.*
import logging4s.core.Delay

trait TaskToDelayInstance:

  given Delay[Task] = new:
    override def delay[A](a: => A): Task[A] = ZIO.attempt(a)

object TaskToDelayInstance extends TaskToDelayInstance
