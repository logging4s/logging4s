package logging4s.zio

import scala.reflect.ClassTag
import zio.Task
import logging4s.core.{LoggingContext, Logging}

import TaskToDelayInstance.given

object LoggingZio:

  def create[S](using ClassTag[S]): Task[Logging[Task]] =
    Logging.create[Task, S]

  def create(name: String): Task[Logging[Task]] =
    Logging.create[Task](name)

  def create[S](context: LoggingContext)(using ClassTag[S]): Task[Logging[Task]] =
    Logging.create[Task, S](context)

  def create(name: String, context: LoggingContext): Task[Logging[Task]] =
    Logging.create[Task](name, context)
