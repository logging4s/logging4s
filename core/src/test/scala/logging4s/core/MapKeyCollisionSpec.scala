package logging4s.core

import org.scalatest.wordspec.AnyWordSpec
import org.scalatest.matchers.should.Matchers

class MapKeyCollisionSpec extends AnyWordSpec, Matchers:

  "A map rendered as a JSON object" must:
    "suffix keys whose JSON forms collide instead of emitting a key twice" in:
      Loggable[Map[Option[String], Int]].json(Map(None -> 1, Some("null") -> 2)) shouldEqual """{"null":1,"null_2":2}"""

    "skip a suffix another key already uses" in:
      val map = Map[Option[String], Int](None -> 1, Some("null_2") -> 2, Some("null") -> 3)

      Loggable[Map[Option[String], Int]].json(map) shouldEqual """{"null":1,"null_2":2,"null_3":3}"""

    "leave distinct keys untouched" in:
      Loggable[Map[String, Int]].json(Map("a" -> 1, "b" -> 2)) shouldEqual """{"a":1,"b":2}"""
