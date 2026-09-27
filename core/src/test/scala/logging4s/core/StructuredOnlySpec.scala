package logging4s.core

import org.scalatest.wordspec.AnyWordSpec
import org.scalatest.matchers.should.Matchers

import logging4s.core.config.LoggableEncodingConfig
import logging4s.core.syntax.all.*

class StructuredOnlySpec extends AnyWordSpec, Matchers:

  private val values = Seq(7.asLogValue("count"))

  "LogMessage.render" must:
    "append the values by default" in:
      given LoggableEncodingConfig = LoggableEncodingConfig()

      LogMessage.render("event", None, values) shouldEqual "event: count -> (7)"

    "leave the message as static text when values are not included" in:
      given LoggableEncodingConfig = LoggableEncodingConfig(includeValuesInMessage = false)

      LogMessage.render("event", None, values) shouldEqual "event"

    "still describe the cause when values are not included" in:
      given LoggableEncodingConfig = LoggableEncodingConfig(includeValuesInMessage = false)

      LogMessage.render("event", Some(new RuntimeException("boom")), values) shouldEqual
        "event: class=java.lang.RuntimeException, message=boom"

    "not render a value's plain form at all when values are excluded" in:
      given LoggableEncodingConfig = LoggableEncodingConfig(includeValuesInMessage = false)

      Instrumented.plain = 0
      Instrumented.json = 0

      import Instrumented.given

      LogMessage.render("event", None, Seq(Instrumented.Value("x").asLogValue)) shouldEqual "event"

      Instrumented.plain shouldEqual 0

  "LogMessage.renderAll" must:
    "ignore the setting, for channels that have nowhere else to put values" in:
      given LoggableEncodingConfig = LoggableEncodingConfig(includeValuesInMessage = false)

      LogMessage.renderAll("event", None, values) shouldEqual "event: count -> (7)"
