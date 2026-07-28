package logging4s.kyo

import kyo.{Maybe, Text}

import logging4s.core.Loggable

trait DataInstances:

  private given Loggable[Unit] = new:
    override def key: String            = "value"
    override def plain(u: Unit): String = ""
    override def json(u: Unit): String  = ""

  given [T: Loggable as L] => Loggable[Maybe[T]] =
    Loggable[Either[Unit, T]].contramap(_.toRight(()), L.key)

  given Loggable[Text] =
    val L = Loggable[String]
    L.contramap(_.show, L.key)

object DataInstances extends DataInstances
