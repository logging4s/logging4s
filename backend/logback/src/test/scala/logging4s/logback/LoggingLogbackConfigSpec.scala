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

import logging4s.core.{JsonString, Logging, LoggableValue, PlainString, ValueKey}
import logging4s.core.config.{KeyNameStyle, LoggableEncodingConfig, PlainValuesStyle}

import LogbackInstances.given

class LoggingLogbackConfigSpec extends AnyWordSpec, Matchers:

  LogbackWarmup.touch()

  // A user-declared config in scope. The factory captures it when the logger is created, proving the
  // aggregation-side styles (keyNameStyle, plainValuesStyle) reach the real backend output.
  private given LoggableEncodingConfig =
    LoggableEncodingConfig(keyNameStyle = KeyNameStyle.KebabCase, plainValuesStyle = PlainValuesStyle.Logfmt)

  private val mapper = new ObjectMapper()

  private def capture(loggerName: String)(run: Logging[Try] => Unit): (String, JsonNode) =
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
      (event.getFormattedMessage, mapper.readTree(encoder.encode(event)))
    }

  "Logging backed by logback, with a user-provided LoggableEncodingConfig" must:
    "apply keyNameStyle and plainValuesStyle to the rendered plain message" in:
      val (message, _) = capture("LoggingLogbackConfigSpec-plain") { logging =>
        logging.info(
          "Saved",
          LoggableValue(ValueKey("userName"), PlainString("John"), JsonString("\"John\"")),
          LoggableValue(ValueKey("retryCount"), PlainString("3"), JsonString("3")),
        )
      }

      message shouldEqual "Saved: user-name=John retry-count=3"

    "normalize keys in the structured JSON output too" in:
      val (_, json) = capture("LoggingLogbackConfigSpec-json") { logging =>
        logging.info("Saved", LoggableValue(ValueKey("userName"), PlainString("John"), JsonString("\"John\"")))
      }

      json.has("userName") shouldEqual false
      json.get("user-name").asText() shouldEqual "John"

    "normalize context keys once and keep them alongside call-site values" in:
      val (message, _) = capture("LoggingLogbackConfigSpec-context") { logging =>
        logging
          .withContextValues(LoggableValue(ValueKey("sessionId"), PlainString("abc"), JsonString("\"abc\"")))
          .info("Hello", LoggableValue(ValueKey("userName"), PlainString("John"), JsonString("\"John\"")))
      }

      message shouldEqual "Hello: session-id=abc user-name=John"
