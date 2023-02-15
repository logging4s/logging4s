package logging4s.core

import org.scalatest.wordspec.AnyWordSpec
import org.scalatest.matchers.should.Matchers

class LoggableSpec extends AnyWordSpec with Matchers:

  "Loggable" must {

    "auto summon Seq and List instances" in {
      Loggable[Seq[String]].key shouldEqual "values"
      Loggable[Seq[String]].plain(Seq("a", "b", "c")) shouldEqual "[a,b,c]"
      Loggable[Seq[String]].json(Seq("a", "b", "c")) shouldEqual """["a","b","c"]"""
    }

    "right rename key with extension" in {
      import logging4s.core.syntax.*

      "data".rename("test").key shouldEqual "test"
    }

  }
