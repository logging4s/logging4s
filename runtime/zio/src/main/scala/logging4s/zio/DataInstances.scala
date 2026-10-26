package logging4s.zio

import zio.NonEmptyChunk

import logging4s.core.Loggable

trait DataInstances:

  given NonEmptyChunkLoggable: [T: Loggable] => Loggable[NonEmptyChunk[T]] =
    Loggable[Vector[T]].contramap(_.toChunk.toVector)

object DataInstances extends DataInstances
