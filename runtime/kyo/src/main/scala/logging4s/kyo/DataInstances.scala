package logging4s.kyo

import kyo.Maybe

import logging4s.core.Loggable

trait DataInstances:

  given MaybeLoggable: [T: Loggable] => Loggable[Maybe[T]] =
    Loggable[Option[T]].contramap(_.toOption)

object DataInstances extends DataInstances
