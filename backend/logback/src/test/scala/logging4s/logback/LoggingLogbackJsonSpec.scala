package logging4s.logback

import scala.jdk.CollectionConverters.*
import scala.util.Try

import ch.qos.logback.classic.{Level, Logger as LogbackLogger}
import ch.qos.logback.classic.spi.ILoggingEvent
import ch.qos.logback.core.read.ListAppender
import net.logstash.logback.encoder.LogstashEncoder
import org.slf4j.LoggerFactory

import com.fasterxml.jackson.databind.{JsonNode, ObjectMapper}

import org.scalatest.matchers.should.Matchers
import org.scalatest.wordspec.AnyWordSpec

import logging4s.core.{JsonString, Loggable, Logging, LoggableValue, PlainString, ValueKey}

import LogbackInstances.given

object LoggingLogbackJsonSpec:
  // logback's Logger/LoggerContext are process-wide singletons shared across every project's tests in the same
  // sbt test JVM; serialize access to the dynamic appender-attach/detach sequence below.
  private val appenderLock = new Object

class LoggingLogbackJsonSpec extends AnyWordSpec with Matchers:

  LogbackWarmup.touch()

  private val mapper = new ObjectMapper()

  private def captureJson(loggerName: String)(run: Logging[Try] => Unit): JsonNode =
    LoggingLogbackJsonSpec.appenderLock.synchronized {
      val logbackLogger = LoggerFactory.getLogger(loggerName).asInstanceOf[LogbackLogger]
      logbackLogger.setLevel(Level.ALL)

      val context = logbackLogger.getLoggerContext

      val encoder = new LogstashEncoder()
      encoder.setContext(context)
      encoder.start()

      val appender = new ListAppender[ILoggingEvent]()
      appender.setContext(context)
      appender.start()

      logbackLogger.addAppender(appender)
      try
        val logging = Logging.createTry(loggerName).get
        run(logging)
      finally logbackLogger.detachAppender(appender)

      val event = appender.list.asScala.last
      mapper.readTree(encoder.encode(event))
    }

  "Logging backed by logback, actually encoded to JSON" must:
    "produce a real nested JSON object, not a double-encoded string" in:
      val user = LoggableValue(ValueKey("user"), PlainString("id=1, name=John"), JsonString("""{"id":1,"name":"John"}"""))

      val json = captureJson("LoggingLogbackJsonSpec-object") { logging =>
        logging.info("User created", user)
      }

      json.get("user").isObject shouldEqual true
      json.get("user").get("id").asInt() shouldEqual 1
      json.get("user").get("name").asText() shouldEqual "John"
      json.get("message").asText() should include("User created")

    "produce a real JSON array, not a double-encoded string" in:
      val tags = LoggableValue(ValueKey("tags"), PlainString("[a,b]"), JsonString("""["a","b"]"""))

      val json = captureJson("LoggingLogbackJsonSpec-array") { logging =>
        logging.info("Tagged", tags)
      }

      json.get("tags").isArray shouldEqual true
      json.get("tags").get(0).asText() shouldEqual "a"
      json.get("tags").get(1).asText() shouldEqual "b"

    "suffix duplicated keys instead of overwriting them" in:
      val json = captureJson("LoggingLogbackJsonSpec-duplicates") { logging =>
        logging.info(
          "duplicate keys",
          LoggableValue(ValueKey("k"), PlainString("1"), JsonString("1")),
          LoggableValue(ValueKey("k"), PlainString("2"), JsonString("2")),
        )
      }

      json.get("k").asInt() shouldEqual 1
      json.get("k_2").asInt() shouldEqual 2

    "attach values from withContext alongside call-site values" in:
      val json = captureJson("LoggingLogbackJsonSpec-context") { logging =>
        logging
          .withContextValues(LoggableValue(ValueKey("session"), PlainString("abc"), JsonString("\"abc\"")))
          .info("Hello", LoggableValue(ValueKey("user"), PlainString("John"), JsonString("\"John\"")))
      }

      json.get("session").asText() shouldEqual "abc"
      json.get("user").asText() shouldEqual "John"

    "encode a redacted value as a masked JSON string, never the real value" in:
      val password = Loggable[String].redacted()
      val secret   = "hunter2"
      val value    = LoggableValue(ValueKey("password"), password.plain(secret), password.json(secret))

      val json = captureJson("LoggingLogbackJsonSpec-redacted") { logging =>
        logging.info("Login", value)
      }

      json.get("password").isTextual shouldEqual true
      json.get("password").asText() shouldEqual "***"
      json.toString should not include secret
