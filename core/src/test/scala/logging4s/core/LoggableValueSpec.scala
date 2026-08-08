package logging4s.core

import org.scalatest.wordspec.AnyWordSpec
import org.scalatest.matchers.should.Matchers

class LoggableValueSpec extends AnyWordSpec, Matchers:

  "LoggableValue.deduplicateKeys" must:
    "keep unique keys untouched and in order" in:
      val values = Seq(
        LoggableValue(ValueKey("a"), PlainString("1"), JsonString("1")),
        LoggableValue(ValueKey("b"), PlainString("2"), JsonString("2")),
        LoggableValue(ValueKey("c"), PlainString("3"), JsonString("3")),
      )

      LoggableValue.deduplicateKeys(values) shouldEqual values

    "keep the first occurrence of a duplicated key unsuffixed and suffix the rest from _2" in:
      val values = Seq(
        LoggableValue(ValueKey("k"), PlainString("1"), JsonString("1")),
        LoggableValue(ValueKey("k"), PlainString("2"), JsonString("2")),
        LoggableValue(ValueKey("k"), PlainString("3"), JsonString("3")),
      )

      val result = LoggableValue.deduplicateKeys(values)

      result.map(_.key) shouldEqual Seq("k", "k_2", "k_3")
      result.map(_.plain) shouldEqual Seq("1", "2", "3")

    "preserve original order when duplicates are interleaved with other keys" in:
      val values = Seq(
        LoggableValue(ValueKey("k"), PlainString("1"), JsonString("1")),
        LoggableValue(ValueKey("other"), PlainString("x"), JsonString("x")),
        LoggableValue(ValueKey("k"), PlainString("2"), JsonString("2")),
      )

      val result = LoggableValue.deduplicateKeys(values)

      result.map(_.key) shouldEqual Seq("k", "other", "k_2")
