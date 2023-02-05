package logging4s.cats

import scala.reflect.ClassTag
import cats.effect.kernel.Sync
import logging4s.core.{LoggingContext, Logging as CoreLogging}

import SyncToDelayInstance.given

object Logging:

  def create[F[*]: Sync, S](using ClassTag[S]): F[CoreLogging[F]] =
    CoreLogging.create[F, S]

  def create[F[*]: Sync](name: String): F[CoreLogging[F]] =
    CoreLogging.create[F](name)

  def create[F[*]: Sync, S](context: LoggingContext)(using ClassTag[S]): F[CoreLogging[F]] =
    CoreLogging.create[F, S](context)

  def create[F[*]: Sync](name: String, context: LoggingContext): F[CoreLogging[F]] =
    CoreLogging.create[F](name, context)
