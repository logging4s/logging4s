package logging4s.zio

import scala.reflect.ClassTag
import zio.Task
import logging4s.core.{LoggingContext, Logging, LoggingFactory}

import TaskToDelayInstance.given

object LoggingZio:

  def create[S](using factory: LoggingFactory, S: ClassTag[S]): Task[Logging[Task]] =
    Logging.create[Task, S]

  def create(name: String)(using factory: LoggingFactory): Task[Logging[Task]] =
    Logging.create[Task](name)

  def create[S](context: LoggingContext)(using factory: LoggingFactory, S: ClassTag[S]): Task[Logging[Task]] =
    Logging.create[Task, S](context)

  def create(name: String, context: LoggingContext)(using factory: LoggingFactory): Task[Logging[Task]] =
    Logging.create[Task](name, context)
