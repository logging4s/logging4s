package logging4s.zio

import scala.reflect.ClassTag
import zio.Task
import logging4s.core.{LoggingContext, Logging as CoreLogging}

import TaskToDelayInstance.given

object Logging:

  def create[S](using ClassTag[S]): Task[CoreLogging[Task]] =
    CoreLogging.create[Task, S]

  def create(name: String): Task[CoreLogging[Task]] =
    CoreLogging.create[Task](name)

  def create[S](context: LoggingContext)(using ClassTag[S]): Task[CoreLogging[Task]] =
    CoreLogging.create[Task, S](context)

  def create(name: String, context: LoggingContext): Task[CoreLogging[Task]] =
    CoreLogging.create[Task](name, context)
