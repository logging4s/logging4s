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
        1 -> "v1",
        2 -> "v2",
        3 -> "v3",
      )

      val lv = data.asLogValue("data")

      lv.key shouldEqual "data"
      lv.plain shouldEqual "[(1, v1),(2, v2),(3, v3)]"
      lv.json shouldEqual """[[1,"v1"],[2,"v2"],[3,"v3"]]"""
