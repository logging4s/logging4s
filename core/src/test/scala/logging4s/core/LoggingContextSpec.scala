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
