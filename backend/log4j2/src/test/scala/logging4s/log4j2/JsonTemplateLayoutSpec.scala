package logging4s.log4j2

import org.scalatest.wordspec.AnyWordSpec
import org.scalatest.matchers.should.Matchers

import org.apache.logging.log4j.Level
import org.apache.logging.log4j.core.impl.Log4jLogEvent
import org.apache.logging.log4j.core.config.DefaultConfiguration
import org.apache.logging.log4j.layout.template.json.JsonTemplateLayout

class JsonTemplateLayoutSpec extends AnyWordSpec, Matchers:

  private def render(template: String): String =
    val message = LoggableMapMessage(Seq("user" -> """{"id":1}"""), "user created: user -> (id -> (1))")

    val layout = JsonTemplateLayout
      .newBuilder()
      .setConfiguration(new DefaultConfiguration())
      .setEventTemplate(template)
      .build()

    layout.toSerializable(Log4jLogEvent.newBuilder().setLevel(Level.INFO).setMessage(message).build())

  "The message resolver in object mode" must:
    "splice values as real nested JSON, and carry no human-readable text" in:
      val json = render("""{"message":{"$resolver":"message"}}""")

      json should include("""{"id":1}""")
      json should not include "user created"

  "The documented template" must:
    "keep the human-readable text and the nested values in separate fields" in:
      val json = render("""{"message":{"$resolver":"message","stringified":true},"data":{"$resolver":"message"}}""")

      json should include("user created: user -> (id -> (1))")
      json should include(""""user":{"id":1}""")

  "The message" must:
    "render its human-readable text through the StringBuilder path layouts actually use" in:
      val message = LoggableMapMessage(Seq("user" -> """{"id":1}"""), "user created")
      val buffer  = new java.lang.StringBuilder

      message.formatTo(buffer)

      buffer.toString shouldEqual "user created"
      message.getFormattedMessage shouldEqual "user created"
