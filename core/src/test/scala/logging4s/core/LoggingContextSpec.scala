package logging4s.core

import org.scalatest.wordspec.AnyWordSpec
import org.scalatest.matchers.should.Matchers

class LoggingContextSpec extends AnyWordSpec with Matchers:

  "LoggingContext" must:
    "start empty" in:
      LoggingContext.empty.values shouldEqual Seq.empty

    "wrap a single value via apply" in:
      val value = LoggableValue("key", "plain", "json")

      LoggingContext(value).values shouldEqual Seq(value)

    "concatenate values in order with +" in:
      val first  = LoggingContext(LoggableValue("a", "1", "1"))
      val second = LoggingContext(LoggableValue("b", "2", "2"))

      (first + second).values shouldEqual Seq(
        LoggableValue("a", "1", "1"),
        LoggableValue("b", "2", "2"),
      )

    "leave both sides unchanged after +" in:
      val first  = LoggingContext(LoggableValue("a", "1", "1"))
      val second = LoggingContext(LoggableValue("b", "2", "2"))

      first + second

      first.values shouldEqual Seq(LoggableValue("a", "1", "1"))
      second.values shouldEqual Seq(LoggableValue("b", "2", "2"))
