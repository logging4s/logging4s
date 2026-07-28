package logging4s.cats

import cats.data.{Chain, Ior, NonEmptyList, NonEmptyMap, NonEmptySet, NonEmptyVector}

import logging4s.core.Loggable

trait DataInstances:

  given [T: Loggable]: Loggable[NonEmptyList[T]] =
    val list = Loggable[List[T]]
    list.contramap(_.toList, list.key)

  given [T: Loggable]: Loggable[NonEmptyVector[T]] =
    val vector = Loggable[Vector[T]]
    vector.contramap(_.toVector, vector.key)

  given [T: Loggable]: Loggable[NonEmptySet[T]] =
    val set = Loggable[Set[T]]
    set.contramap(_.toSortedSet, set.key)

  given [K: Loggable, V: Loggable]: Loggable[NonEmptyMap[K, V]] =
    val map = Loggable[Map[K, V]]
    map.contramap(_.toSortedMap, map.key)

  given [T: Loggable]: Loggable[Chain[T]] =
    val list = Loggable[List[T]]
    list.contramap(_.toList, list.key)

  given [A, B](using AL: Loggable[A], BL: Loggable[B]): Loggable[Ior[A, B]] =
    new:
      override def key: String                   = if AL.key == BL.key then AL.key else s"${AL.key}_${BL.key}"
      override def plain(ior: Ior[A, B]): String = ior.fold(AL.plain, BL.plain, (a, b) => s"(${AL.plain(a)}, ${BL.plain(b)})")
      override def json(ior: Ior[A, B]): String  = ior.fold(AL.json, BL.json, (a, b) => s"[${AL.json(a)},${BL.json(b)}]")

object DataInstances extends DataInstances
