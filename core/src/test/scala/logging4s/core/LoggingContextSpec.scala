package logging4s.core

import org.scalatest.wordspec.AnyWordSpec
import org.scalatest.matchers.should.Matchers

class LoggingContextSpec extends AnyWordSpec, Matchers:

  "LoggingContext" must:
    "start empty" in:
      LoggingContext.empty.values shouldEqual Seq.empty

    "wrap a single value via apply" in:
      val value = LoggableValue(ValueKey("key"), PlainString("plain"), JsonString("json"))

      LoggingContext(value).values shouldEqual Seq(value)

    "concatenate values in order with +" in:
      val first  = LoggingContext(LoggableValue(ValueKey("a"), PlainString("1"), JsonString("1")))
      val second = LoggingContext(LoggableValue(ValueKey("b"), PlainString("2"), JsonString("2")))

      (first + second).values shouldEqual Seq(
        LoggableValue(ValueKey("a"), PlainString("1"), JsonString("1")),
        LoggableValue(ValueKey("b"), PlainString("2"), JsonString("2")),
      )

    "leave both sides unchanged after +" in:
      val first  = LoggingContext(LoggableValue(ValueKey("a"), PlainString("1"), JsonString("1")))
      val second = LoggingContext(LoggableValue(ValueKey("b"), PlainString("2"), JsonString("2")))

      first + second

      first.values shouldEqual Seq(LoggableValue(ValueKey("a"), PlainString("1"), JsonString("1")))
      second.values shouldEqual Seq(LoggableValue(ValueKey("b"), PlainString("2"), JsonString("2")))

    "let a later context value replace an earlier one with the same key" in:
      val first  = LoggingContext(LoggableValue(ValueKey("user"), PlainString("1"), JsonString("1")))
      val second = LoggingContext(LoggableValue(ValueKey("user"), PlainString("2"), JsonString("2")))

      (first + second).values shouldEqual Seq(LoggableValue(ValueKey("user"), PlainString("2"), JsonString("2")))

    "keep the position of surviving keys and append the new ones" in:
      val first  = LoggingContext(
        LoggableValue(ValueKey("a"), PlainString("1"), JsonString("1")),
        LoggableValue(ValueKey("b"), PlainString("2"), JsonString("2")),
      )
      val second = LoggingContext(
        LoggableValue(ValueKey("b"), PlainString("9"), JsonString("9")),
        LoggableValue(ValueKey("c"), PlainString("3"), JsonString("3")),
      )

      (first + second).values.map(_.key) shouldEqual Seq(ValueKey("a"), ValueKey("b"), ValueKey("c"))
      (first + second).values.map(_.plain) shouldEqual Seq(PlainString("1"), PlainString("9"), PlainString("3"))

    "drop an earlier duplicate inside a single context" in:
      val context = LoggingContext(
        LoggableValue(ValueKey("user"), PlainString("1"), JsonString("1")),
        LoggableValue(ValueKey("user"), PlainString("2"), JsonString("2")),
      )

      context.values shouldEqual Seq(LoggableValue(ValueKey("user"), PlainString("2"), JsonString("2")))
