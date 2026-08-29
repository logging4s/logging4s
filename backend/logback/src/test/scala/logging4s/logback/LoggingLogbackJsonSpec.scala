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
import logging4s.core.syntax.all.*

import LogbackInstances.given

final case class Nickname(id: Int, nick: Option[String]) derives Loggable

object LoggingLogbackJsonSpec:
  private[logback] val appenderLock = new Object

class LoggingLogbackJsonSpec extends AnyWordSpec with Matchers:

  LogbackWarmup.touch()

  private val mapper = new ObjectMapper()

  private def captureJson(loggerName: String)(run: Logging[Try] => Unit): JsonNode =
    LoggingLogbackJsonSpec.appenderLock.synchronized {
      val logbackLogger = LoggerFactory.getLogger(loggerName).asInstanceOf[LogbackLogger]
      logbackLogger.setLevel(Level.TRACE)

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

    "encode a summoned tuple value as a JSON array by default" in:
      val json = captureJson("LoggingLogbackJsonSpec-tuple") { logging =>
        logging.info("Paired", (1, "a").asLogValue("pair"))
      }

      json.get("pair").isArray shouldEqual true
      json.get("pair").get(0).asInt() shouldEqual 1
      json.get("pair").get(1).asText() shouldEqual "a"

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

    "escape special characters so the structured JSON stays valid and round-trips" in:
      val nasty = "line1\nline2 \"q\" path C:\\x\ttab"

      val json = captureJson("LoggingLogbackJsonSpec-escaping") { logging =>
        logging.info("x", nasty.asLogValue("text"))
      }

      json.get("text").asText() shouldEqual nasty

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

    "keep the line parseable when the call site passes no values" in:
      val noValues = Seq.empty[LoggableValue]

      val json = captureJson("LoggingLogbackJsonSpec-no-values") { logging =>
        logging.info("nothing to add", noValues*)
      }

      json.get("message").asText() shouldEqual "nothing to add"

    "render an absent optional field as JSON null instead of breaking the line" in:
      val json = captureJson("LoggingLogbackJsonSpec-optional") { logging =>
        logging.info("Nickname", Nickname(1, None).asLogValue)
      }

      json.get("nickname").isObject shouldEqual true
      json.get("nickname").get("id").asInt() shouldEqual 1
      json.get("nickname").get("nick").isNull shouldEqual true

    "attach context values to a message logged without call-site values" in:
      val json = captureJson("LoggingLogbackJsonSpec-context-only") { logging =>
        logging
          .withContextValues(LoggableValue(ValueKey("session"), PlainString("abc"), JsonString("\"abc\"")))
          .info("no call-site values")
      }

      json.get("session").asText() shouldEqual "abc"

    "attach an interpolated throwable as a structured field" in:
      val json = captureJson("LoggingLogbackJsonSpec-throwable") { logging =>
        given Logging[Try] = logging
        val boom           = new IllegalStateException("kaboom")

        val _ = error"request failed $boom"
      }

      json.get("boom").isObject shouldEqual true
      json.get("boom").get("class").asText() shouldEqual "java.lang.IllegalStateException"
      json.get("boom").get("message").asText() shouldEqual "kaboom"

    "attach the call site as a source field" in:
      val json = captureJson("LoggingLogbackJsonSpec-source") { logging =>
        logging.info("with position")
      }

      json.get("source").asText() should startWith("LoggingLogbackJsonSpec.scala:")

    "attach the call site for interpolated messages too" in:
      val json = captureJson("LoggingLogbackJsonSpec-source-interpolated") { logging =>
        given Logging[Try] = logging
        val user           = 7

        val _ = info"created $user"
      }

      json.get("source").asText() should startWith("LoggingLogbackJsonSpec.scala:")
      json.get("user").asInt() shouldEqual 7

    "let a call-site value override a context value with the same key" in:
      val json = captureJson("LoggingLogbackJsonSpec-override") { logging =>
        logging
          .withContextValues(LoggableValue(ValueKey("user"), PlainString("ctx"), JsonString("\"ctx\"")))
          .info("Hello", LoggableValue(ValueKey("user"), PlainString("call"), JsonString("\"call\"")))
      }

      json.get("user").asText() shouldEqual "call"
      json.has("user_2") shouldEqual false
