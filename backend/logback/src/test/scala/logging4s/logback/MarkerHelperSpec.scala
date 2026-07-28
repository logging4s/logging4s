package logging4s.logback

import org.scalatest.matchers.should.Matchers
import org.scalatest.wordspec.AnyWordSpec

import logging4s.core.LoggableValue

class MarkerHelperSpec extends AnyWordSpec with Matchers:

  "MarkerHelper.fromLoggable" must:
    "build a marker for a single value" in:
      noException should be thrownBy MarkerHelper.fromLoggable(LoggableValue("a", "1", "1"))

    "build a combined marker for a sequence with duplicated keys" in:
      val values = Seq(
        LoggableValue("k", "1", "1"),
        LoggableValue("k", "2", "2"),
      )

      noException should be thrownBy MarkerHelper.fromLoggable(values)
