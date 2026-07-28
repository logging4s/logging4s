package logging4s.cats

import scala.reflect.ClassTag
import cats.effect.kernel.Sync
import logging4s.core.{LoggingContext, Logging, LoggingFactory}

import SyncToDelayInstance.given

object LoggingCats:

  def create[F[*]: Sync, S](using factory: LoggingFactory, S: ClassTag[S]): F[Logging[F]] =
    Logging.create[F, S]

  def create[F[*]: Sync](name: String)(using factory: LoggingFactory): F[Logging[F]] =
    Logging.create[F](name)

  def create[F[*]: Sync, S](context: LoggingContext)(using factory: LoggingFactory, S: ClassTag[S]): F[Logging[F]] =
    Logging.create[F, S](context)

  def create[F[*]: Sync](name: String, context: LoggingContext)(using factory: LoggingFactory): F[Logging[F]] =
    Logging.create[F](name, context)
