package logging4s.core

import org.scalatest.wordspec.AnyWordSpec
import org.scalatest.matchers.should.Matchers

class LoggableSpec extends AnyWordSpec with Matchers:

  "Loggable" must {
    given Loggable[String] with
      override def key(a: String): String = "value"
      override def json(a: String): String = s""""$a""""
      override def plain(a: String): String = a

    "auto summon Seq and List instances" in {
      Loggable[Seq[String]].json(Seq("a", "b", "c")) shouldEqual """["a","b","c"]"""
      Loggable[Seq[String]].plain(Seq("a", "b", "c")) shouldEqual "[a,b,c]"
      Loggable[Seq[String]].key(Seq("a", "b", "c")) shouldEqual "values"

      Loggable[List[String]].json(List("a", "b", "c")) shouldEqual """["a","b","c"]"""
      Loggable[List[String]].plain(List("a", "b", "c")) shouldEqual "[a,b,c]"
      Loggable[List[String]].key(List("a", "b", "c")) shouldEqual "values"
    }

    "right rename key with extension" in {
      import logging4s.core.Loggable.extensions.*

      "data".renameLoggable("test").key("") shouldEqual "test"
    }

  }
