package logging4s.core

import org.scalatest.wordspec.AnyWordSpec
import org.scalatest.matchers.should.Matchers

import logging4s.core.syntax.*

class LoggableSpec extends AnyWordSpec with Matchers:

  "Loggable" must:
    "auto summon Seq and List instances" in:
      Loggable[Seq[String]].key shouldEqual "values"
      Loggable[Seq[String]].plain(Seq("a", "b", "c")) shouldEqual "[a,b,c]"
      Loggable[Seq[String]].json(Seq("a", "b", "c")) shouldEqual """["a","b","c"]"""

    "right rename key with extension" in:
      "data".rename("test").key shouldEqual "test"

    "right convert collection of tuples" in:
      val data = List(
        1 -> Str("v1"),
        2 -> Str("v2"),
        3 -> Str("v3")
      )

      val lv = data.asLogValue("data")

      lv.key shouldEqual "data"
      lv.plain shouldEqual "data -> [(value=1, str=v1), (value=2, str=v2), (value=3, str=v3)]"
      lv.json shouldEqual """{"data":[{"value":1,"str":"v1"},{"value":2,"str":"v2"},{"value":3,"str":"v3"}]}"""
