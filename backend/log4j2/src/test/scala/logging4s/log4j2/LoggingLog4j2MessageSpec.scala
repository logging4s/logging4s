package logging4s.log4j2

import scala.util.Try

import org.apache.logging.log4j.{Level, LogManager, ThreadContext}
import org.apache.logging.log4j.core.{LogEvent, LoggerContext}
import org.apache.logging.log4j.core.appender.AbstractAppender
import org.apache.logging.log4j.core.config.{AppenderRef, Configuration, LoggerConfig, Property}
import org.apache.logging.log4j.message.{Message, MapMessage}

import org.scalatest.matchers.should.Matchers
import org.scalatest.wordspec.AnyWordSpec

import logging4s.core.{JsonString, Logging, LoggableValue, PlainString, ValueKey}

import Log4j2Instances.given

final class MessageCapturingAppender extends AbstractAppender("message-capturing-appender", null, null, false, Property.EMPTY_ARRAY):
  private var lastCapturedMessage: Option[Message] = None

  override def append(event: LogEvent): Unit =
    lastCapturedMessage = Some(event.getMessage)

  def lastMessage: Message = lastCapturedMessage.getOrElse(throw new IllegalStateException("no message captured"))

object LoggingLog4j2MessageSpec:
  // Log4j2's LoggerContext/Configuration are process-wide singletons; addLogger/updateLoggers here race against any
  // other test doing the same dynamic reconfiguration concurrently, so serialize access to it.
  private val configurationLock = new Object

class LoggingLog4j2MessageSpec extends AnyWordSpec with Matchers:

  Log4j2Warmup.touch()

  private def captureMessage(loggerName: String)(run: Logging[Try] => Unit): Message =
    LoggingLog4j2MessageSpec.configurationLock.synchronized {
      val context: LoggerContext       = LogManager.getContext(false).asInstanceOf[LoggerContext]
      val configuration: Configuration = context.getConfiguration

      val appender = new MessageCapturingAppender
      appender.start()
      configuration.addAppender(appender)

      val appenderRef  = AppenderRef.createAppenderRef("message-capturing-appender", null, null)
      val loggerConfig = LoggerConfig.createLogger(
        false,
        Level.ALL,
        loggerName,
        "true",
        Array(appenderRef),
        null,
        configuration,
        null
      )
      loggerConfig.addAppender(appender, null, null)
      configuration.addLogger(loggerName, loggerConfig)
      context.updateLoggers()

      try
        val logging = Logging.createTry(loggerName).get
        run(logging)
        appender.lastMessage
      finally
        configuration.removeLogger(loggerName)
        context.updateLoggers()
    }

  "Logging backed by log4j2" must:
    "pass structured values directly as a MapMessage argument, not via ThreadContext" in:
      val message = captureMessage("LoggingLog4j2MessageSpec-object") { logging =>
        logging.info("User created", LoggableValue(ValueKey("user"), PlainString("John"), JsonString("\"John\"")))
      }

      message shouldBe a[MapMessage[?, ?]]
      message.getFormattedMessage should include("User created")

      val mapMessage = message.asInstanceOf[MapMessage[?, String]]
      mapMessage.get("user") shouldEqual "\"John\""

    "suffix duplicated keys in the message data" in:
      val message = captureMessage("LoggingLog4j2MessageSpec-duplicates") { logging =>
        logging.info(
          "duplicate keys",
          LoggableValue(ValueKey("k"), PlainString("1"), JsonString("1")),
          LoggableValue(ValueKey("k"), PlainString("2"), JsonString("2")),
        )
      }

      val mapMessage = message.asInstanceOf[MapMessage[?, String]]
      mapMessage.get("k") shouldEqual "1"
      mapMessage.get("k_2") shouldEqual "2"

    "attach values from withContext alongside call-site values" in:
      val message = captureMessage("LoggingLog4j2MessageSpec-withcontext") { logging =>
        logging
          .withContextValues(LoggableValue(ValueKey("session"), PlainString("abc"), JsonString("\"abc\"")))
          .info("Hello", LoggableValue(ValueKey("user"), PlainString("John"), JsonString("\"John\"")))
      }

      val mapMessage = message.asInstanceOf[MapMessage[?, String]]
      mapMessage.get("session") shouldEqual "\"abc\""
      mapMessage.get("user") shouldEqual "\"John\""

    "leave ThreadContext untouched" in:
      captureMessage("LoggingLog4j2MessageSpec-no-mdc") { logging =>
        logging.info("hello", LoggableValue(ValueKey("user"), PlainString("John"), JsonString("\"John\"")))
      }

      ThreadContext.get("user") shouldEqual null
