package logging4s.rapid

import rapid.Task

import logging4s.core.Delay

trait TaskToDelayInstance:

  given Delay[Task] = new:
    override def delay[A](a: => A): Task[A] = Task(a)

object TaskToDelayInstance extends TaskToDelayInstance
