package logging4s.rapid

import rapid.Task

import logging4s.core.Delay

trait TaskToDelayInstance:

  given RapidTaskDelay: Delay[Task] = new:
    override def delay[A](a: => A): Task[A] = Task(a)
    override val unit: Task[Unit]           = Task

object TaskToDelayInstance extends TaskToDelayInstance
