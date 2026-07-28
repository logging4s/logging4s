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

import logging4s.core.{Logging, LoggableValue}

import instances.given

class LoggingLogbackJsonSpec extends AnyWordSpec with Matchers:

  private val mapper = new ObjectMapper()

  private def captureJson(loggerName: String)(run: Logging[Try] => Unit): JsonNode =
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

  "Logging backed by logback, actually encoded to JSON" must:
    "produce a real nested JSON object, not a double-encoded string" in:
      val user = LoggableValue("user", "id=1, name=John", """{"id":1,"name":"John"}""")

      val json = captureJson("LoggingLogbackJsonSpec-object") { logging =>
        logging.info("User created", user)
      }

      json.get("user").isObject shouldEqual true
      json.get("user").get("id").asInt() shouldEqual 1
      json.get("user").get("name").asText() shouldEqual "John"
      json.get("message").asText() should include("User created")

    "produce a real JSON array, not a double-encoded string" in:
      val tags = LoggableValue("tags", "[a,b]", """["a","b"]""")

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
          LoggableValue("k", "1", "1"),
          LoggableValue("k", "2", "2"),
        )
      }

      json.get("k").asInt() shouldEqual 1
      json.get("k_2").asInt() shouldEqual 2

    "attach values from withContext alongside call-site values" in:
      val json = captureJson("LoggingLogbackJsonSpec-context") { logging =>
        logging.withContext(LoggableValue("session", "abc", "\"abc\"")).info("Hello", LoggableValue("user", "John", "\"John\""))
      }

      json.get("session").asText() shouldEqual "abc"
      json.get("user").asText() shouldEqual "John"
