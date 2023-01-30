package logging4s.zio

import scala.reflect.ClassTag
import zio.Task
import logging4s.core.Logging as CoreLogging

import TaskToDelayInstance.given

object Logging:

  def create[S](using ClassTag[S]): Task[CoreLogging[Task]] =
    CoreLogging.create[Task, S]

  def create(name: String): Task[CoreLogging[Task]] =
    CoreLogging.create[Task](name)
