package logging4s.log4j2

import org.apache.logging.log4j.Level
import org.apache.logging.log4j.core.config.DefaultConfiguration
import org.apache.logging.log4j.core.impl.Log4jLogEvent
import org.apache.logging.log4j.layout.template.json.JsonTemplateLayout

import org.scalatest.matchers.should.Matchers
import org.scalatest.wordspec.AnyWordSpec

class LoggableMapMessageJsonTemplateSpec extends AnyWordSpec, Matchers:

  private def render(message: LoggableMapMessage): String =
    val layout = JsonTemplateLayout
      .newBuilder()
      .setConfiguration(new DefaultConfiguration())
      .setEventTemplate("""{"m":{"$resolver":"message"}}""")
      .build()

    val event = Log4jLogEvent
      .newBuilder()
      .setLoggerName("test")
      .setLevel(Level.INFO)
      .setMessage(message)
      .build()

    layout.toSerializable(event)

  "LoggableMapMessage under a JsonTemplateLayout message resolver" must:
    "emit structured values as raw nested JSON, not escaped strings" in:
      val json = render(LoggableMapMessage(Map("user" -> """{"id":1,"name":"John"}"""), "user created"))

      json should include(""""user":{"id":1,"name":"John"}""")
      json should not include "\\\"id\\\""
