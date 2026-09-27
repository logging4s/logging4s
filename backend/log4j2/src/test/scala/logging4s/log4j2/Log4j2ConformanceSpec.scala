package logging4s.log4j2

import scala.util.Try

import org.apache.logging.log4j.message.Message
import org.apache.logging.log4j.core.{LogEvent, LoggerContext}
import org.apache.logging.log4j.core.appender.AbstractAppender
import org.apache.logging.log4j.{Level as Log4jLevel, LogManager}
import org.apache.logging.log4j.core.config.{Configuration, LoggerConfig, Property}

import logging4s.core.Logging
import logging4s.core.conformance.{BackendConformance, CapturedRecord, JsonRecord}

import Log4j2Instances.given

final class ConformanceAppender extends AbstractAppender("conformance-appender", null, null, false, Property.EMPTY_ARRAY):
  private var captured: Option[Message] = None

  override def append(event: LogEvent): Unit = captured = Some(event.getMessage)

  def message: Message = captured.getOrElse(throw new IllegalStateException("no message captured"))

class Log4j2ConformanceSpec extends BackendConformance:

  Log4j2Warmup.touch()

  override def backendName: String = "log4j2"

  override def capture(loggerName: String)(run: Logging[Try] => Unit): CapturedRecord =
    Log4j2ConformanceSpec.configurationLock.synchronized {
      val context: LoggerContext       = LogManager.getContext(false).asInstanceOf[LoggerContext]
      val configuration: Configuration = context.getConfiguration

      val appender = new ConformanceAppender
      appender.start()
      configuration.addAppender(appender)

      val loggerConfig = new LoggerConfig(loggerName, Log4jLevel.TRACE, false)

      loggerConfig.addAppender(appender, null, null)
      configuration.addLogger(loggerName, loggerConfig)
      context.updateLoggers()

      try run(Logging.createTry(loggerName).get)
      finally
        configuration.removeLogger(loggerName)
        appender.stop()
        context.updateLoggers()

      val message = appender.message.asInstanceOf[LoggableMapMessage]

      CapturedRecord(message.getFormattedMessage, JsonRecord.parse(message.getFormattedMessage(Array("JSON"))).fields)
    }

object Log4j2ConformanceSpec:
  private val configurationLock = new Object
