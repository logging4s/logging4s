package logging4s.logback

import java.io.ByteArrayOutputStream

import scala.util.Try

import ch.qos.logback.classic.{Level, Logger as LogbackLogger}
import ch.qos.logback.classic.spi.ILoggingEvent
import ch.qos.logback.core.OutputStreamAppender
import org.slf4j.LoggerFactory

import com.fasterxml.jackson.databind.{JsonNode, ObjectMapper}

import org.scalatest.matchers.should.Matchers
import org.scalatest.wordspec.AnyWordSpec

import logging4s.core.{JsonString, Logging, LoggableValue, PlainString, ValueKey}

import LogbackInstances.given

class Logging4sEncoderSpec extends AnyWordSpec, Matchers:

  LogbackWarmup.touch()

  private val mapper = new ObjectMapper()

  private def capture(loggerName: String)(run: Logging[Try] => Unit): JsonNode =
    LoggingLogbackJsonSpec.appenderLock.synchronized {
      val logbackLogger = LoggerFactory.getLogger(loggerName).asInstanceOf[LogbackLogger]
      logbackLogger.setLevel(Level.ALL)

      val context = logbackLogger.getLoggerContext
      val out     = new ByteArrayOutputStream()

      val encoder = new Logging4sEncoder()
      encoder.setContext(context)
      encoder.start()

      val appender = new OutputStreamAppender[ILoggingEvent]()
      appender.setContext(context)
      appender.setEncoder(encoder)
      appender.setOutputStream(out)
      appender.start()

      logbackLogger.addAppender(appender)
      try
        val logging = Logging.createTry(loggerName).get
        run(logging)
      finally
        appender.stop()
        logbackLogger.detachAppender(appender)

      mapper.readTree(out.toString("UTF-8"))
    }

  "Logging4sEncoder" must:
    "render structured values as real nested JSON, read from the event markers" in:
      val user = LoggableValue(ValueKey("user"), PlainString("id=1, name=John"), JsonString("""{"id":1,"name":"John"}"""))

      val json = capture("Logging4sEncoderSpec-object") { logging =>
        logging.info("User created", user)
      }

      json.get("user").isObject shouldEqual true
      json.get("user").get("id").asInt() shouldEqual 1
      json.get("user").get("name").asText() shouldEqual "John"

    "write the standard envelope fields" in:
      val json = capture("Logging4sEncoderSpec-envelope") { logging =>
        logging.info("hello", LoggableValue(ValueKey("k"), PlainString("v"), JsonString("\"v\"")))
      }

      json.get("level").asText() shouldEqual "INFO"
      json.get("logger").asText() shouldEqual "Logging4sEncoderSpec-envelope"
      json.get("message").asText() should include("hello")
      json.get("@timestamp").isTextual shouldEqual true
      json.get("k").asText() shouldEqual "v"

    "attach several values as distinct fields" in:
      val json = capture("Logging4sEncoderSpec-many") { logging =>
        logging.info(
          "multi",
          LoggableValue(ValueKey("a"), PlainString("1"), JsonString("1")),
          LoggableValue(ValueKey("b"), PlainString("2"), JsonString("2")),
        )
      }

      json.get("a").asInt() shouldEqual 1
      json.get("b").asInt() shouldEqual 2
