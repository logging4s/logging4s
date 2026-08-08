package logging4s.cats

import cats.data.{Chain, Ior, NonEmptyList, NonEmptyMap, NonEmptySet, NonEmptyVector}

import logging4s.core.{JsonString, Loggable, PlainString, ValueKey}

trait DataInstances:

  given NonEmptyListLoggable[T: Loggable]: Loggable[NonEmptyList[T]] =
    Loggable[List[T]].contramap(_.toList)

  given NonEmptyVectorLoggable[T: Loggable]: Loggable[NonEmptyVector[T]] =
    Loggable[Vector[T]].contramap(_.toVector)

  given NonEmptySetLoggable[T: Loggable]: Loggable[NonEmptySet[T]] =
    Loggable[Set[T]].contramap(_.toSortedSet)

  given NonEmptyMapLoggable[K: Loggable, V: Loggable]: Loggable[NonEmptyMap[K, V]] =
    Loggable[Map[K, V]].contramap(_.toSortedMap)

  given ChainLoggable[T: Loggable]: Loggable[Chain[T]] =
    Loggable[List[T]].contramap(_.toList)

  given IorLoggable[A, B](using AL: Loggable[A], BL: Loggable[B]): Loggable[Ior[A, B]] =
    new:
      override def key: ValueKey =
        ValueKey.combine(AL.key, BL.key)

      override def plain(ior: Ior[A, B]): PlainString =
        ior.fold(AL.plain, BL.plain, (a, b) => PlainString.tuple(AL.plain(a), BL.plain(b)))

      override def json(ior: Ior[A, B]): JsonString =
        ior.fold(AL.json, BL.json, (a, b) => JsonString.array(AL.json(a), BL.json(b)))
  end IorLoggable

object DataInstances extends DataInstances
