package logging4s.cats

import scala.reflect.ClassTag
import cats.effect.Sync
import logging4s.core.{LoggingContext, Logging}

import SyncToDelayInstance.given

object LoggingCats:

  def create[F[*]: Sync, S](using ClassTag[S]): F[Logging[F]] =
    Logging.create[F, S]

  def create[F[*]: Sync](name: String): F[Logging[F]] =
    Logging.create[F](name)

  def create[F[*]: Sync, S](context: LoggingContext)(using ClassTag[S]): F[Logging[F]] =
    Logging.create[F, S](context)

  def create[F[*]: Sync](name: String, context: LoggingContext): F[Logging[F]] =
    Logging.create[F](name, context)
