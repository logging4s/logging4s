package logging4s.cats

import org.scalatest.wordspec.AnyWordSpec
import org.scalatest.matchers.should.Matchers

import cats.data.*

import logging4s.core.Loggable

import DataInstances.given

class CatsDataInstancesSpec extends AnyWordSpec, Matchers:

  "DataInstances" must:
    "right summon NonEmptyList instances" in:
      val nel = NonEmptyList.of(1, 2, 3)

      Loggable[NonEmptyList[Int]].key shouldEqual "values"
      Loggable[NonEmptyList[Int]].plain(nel) shouldEqual "[1,2,3]"
      Loggable[NonEmptyList[Int]].json(nel) shouldEqual "[1,2,3]"

    "right summon NonEmptyVector instances" in:
      val nev = NonEmptyVector.of(1, 2, 3)

      Loggable[NonEmptyVector[Int]].plain(nev) shouldEqual "[1,2,3]"
      Loggable[NonEmptyVector[Int]].json(nev) shouldEqual "[1,2,3]"

    "right summon NonEmptySet instances" in:
      val nes = NonEmptySet.of(1, 2, 3)

      Loggable[NonEmptySet[Int]].plain(nes) shouldEqual "[1,2,3]"
      Loggable[NonEmptySet[Int]].json(nes) shouldEqual "[1,2,3]"

    "right summon NonEmptyMap instances" in:
      val nem = NonEmptyMap.of("a" -> 1)

      Loggable[NonEmptyMap[String, Int]].plain(nem) shouldEqual "[(a, 1)]"
      Loggable[NonEmptyMap[String, Int]].json(nem) shouldEqual """[["a",1]]"""

    "right summon Chain instances" in:
      val chain = Chain(1, 2, 3)

      Loggable[Chain[Int]].plain(chain) shouldEqual "[1,2,3]"
      Loggable[Chain[Int]].json(chain) shouldEqual "[1,2,3]"

    "right summon Ior instances" in:
      Loggable[Ior[String, Int]].plain(Ior.left("err")) shouldEqual "err"
      Loggable[Ior[String, Int]].json(Ior.left("err")) shouldEqual "\"err\""

      Loggable[Ior[String, Int]].plain(Ior.right(5)) shouldEqual "5"
      Loggable[Ior[String, Int]].json(Ior.right(5)) shouldEqual "5"

      Loggable[Ior[String, Int]].plain(Ior.both("err", 5)) shouldEqual "(err, 5)"
      Loggable[Ior[String, Int]].json(Ior.both("err", 5)) shouldEqual """["err",5]"""
