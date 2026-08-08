package logging4s.logback

import org.scalatest.matchers.should.Matchers
import org.scalatest.wordspec.AnyWordSpec

import logging4s.core.{JsonString, LoggableValue, PlainString, ValueKey}

class MarkerHelperSpec extends AnyWordSpec, Matchers:

  "MarkerHelper.fromLoggable" must:
    "build a marker for a single value" in:
      noException should be thrownBy MarkerHelper.fromLoggable(LoggableValue(ValueKey("a"), PlainString("1"), JsonString("1")))

    "build a combined marker for a sequence with duplicated keys" in:
      val values = Seq(
        LoggableValue(ValueKey("k"), PlainString("1"), JsonString("1")),
        LoggableValue(ValueKey("k"), PlainString("2"), JsonString("2")),
      )

      noException should be thrownBy MarkerHelper.fromLoggable(values)
