package logging4s.kyo

import kyo.{Maybe, Text}

import logging4s.core.Loggable

trait DataInstances:

  given [T: Loggable as L] => Loggable[Maybe[T]] =
    new:
      override def key: String                = L.key
      override def plain(t: Maybe[T]): String = t.fold("")(L.plain)
      override def json(t: Maybe[T]): String  = t.fold("")(L.json)

  given Loggable[Text] =
    val L = Loggable[String]
    L.contramap(_.show, L.key)

object DataInstances extends DataInstances
