package logging4s.logback

import scala.jdk.CollectionConverters.*
import scala.util.Try

import scala.concurrent.duration.*

import ch.qos.logback.classic.{Level, Logger as LogbackLogger}
import ch.qos.logback.classic.spi.ILoggingEvent
import ch.qos.logback.core.read.ListAppender
import net.logstash.logback.encoder.LogstashEncoder
import org.slf4j.LoggerFactory

import com.fasterxml.jackson.databind.{JsonNode, ObjectMapper}

import org.scalatest.matchers.should.Matchers
import org.scalatest.wordspec.AnyWordSpec

import logging4s.core.Logging
import logging4s.core.config.LoggableEncodingConfig
import logging4s.core.syntax.all.*

import LogbackInstances.given

class LoggingLogbackTupleConfigSpec extends AnyWordSpec, Matchers:

  LogbackWarmup.touch()

  private given LoggableEncodingConfig = LoggableEncodingConfig(jsonTupleAsArray = false)

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

  "Logging backed by logback, with a user-provided jsonTupleAsArray = false" must:
    "encode a summoned tuple value as a JSON object keyed by element type, in the real backend output" in:
      val json = captureJson("LoggingLogbackTupleConfigSpec-object") { logging =>
        logging.info("Paired", (1, 5.seconds).asLogValue("pair"))
      }

      json.get("pair").isObject shouldEqual true
      json.get("pair").get("int").asInt() shouldEqual 1
      json.get("pair").get("time_ms").asInt() shouldEqual 5000
