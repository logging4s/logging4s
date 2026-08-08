package logging4s.kyo

import kyo.{Maybe, Text}

import logging4s.core.Loggable

trait DataInstances:

  given MaybeLoggable: [T: Loggable] => Loggable[Maybe[T]] =
    Loggable[Option[T]].contramap(_.toOption)

  given TextLoggable: Loggable[Text] =
    Loggable[String].contramap(_.show)

object DataInstances extends DataInstances
