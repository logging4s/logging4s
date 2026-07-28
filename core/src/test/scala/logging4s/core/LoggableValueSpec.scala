package logging4s.core

import org.scalatest.matchers.should.Matchers
import org.scalatest.wordspec.AnyWordSpec

class LoggableValueSpec extends AnyWordSpec with Matchers:

  "LoggableValue.deduplicateKeys" must:
    "keep unique keys untouched and in order" in:
      val values = Seq(
        LoggableValue("a", "1", "1"),
        LoggableValue("b", "2", "2"),
        LoggableValue("c", "3", "3"),
      )

      LoggableValue.deduplicateKeys(values) shouldEqual values

    "keep the first occurrence of a duplicated key unsuffixed and suffix the rest from _2" in:
      val values = Seq(
        LoggableValue("k", "1", "1"),
        LoggableValue("k", "2", "2"),
        LoggableValue("k", "3", "3"),
      )

      val result = LoggableValue.deduplicateKeys(values)

      result.map(_.key) shouldEqual Seq("k", "k_2", "k_3")
      result.map(_.plain) shouldEqual Seq("1", "2", "3")

    "preserve original order when duplicates are interleaved with other keys" in:
      val values = Seq(
        LoggableValue("k", "1", "1"),
        LoggableValue("other", "x", "x"),
        LoggableValue("k", "2", "2"),
      )

      val result = LoggableValue.deduplicateKeys(values)

      result.map(_.key) shouldEqual Seq("k", "other", "k_2")
