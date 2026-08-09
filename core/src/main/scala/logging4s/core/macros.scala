package logging4s.core

import scala.compiletime.summonAll

import logging4s.core.config.LoggableEncodingConfig

private[core] object macros:

  inline def deriveTuple[T <: Tuple](using cfg: LoggableEncodingConfig): Loggable[T] =
    val loggables = summonAll[Tuple.Map[T, Loggable]].toList.asInstanceOf[List[Loggable[Any]]]

    new:
      override val key: ValueKey = ValueKey.combine(loggables.map(_.key)*)

      override def plain(t: T): PlainString =
        cfg.plainTupleStyle.render(loggables.zip(t.productIterator.toList).map((l, a) => l.plain(a)))

      override def json(t: T): JsonString =
        val elements = loggables.zip(t.productIterator.toList)
        if cfg.jsonTupleAsArray then JsonString.array(elements.map((l, a) => l.json(a))*)
        else JsonString.obj(elements.map((l, a) => l.key.value -> l.json(a))*)
