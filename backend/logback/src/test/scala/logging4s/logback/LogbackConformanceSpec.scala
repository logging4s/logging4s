package logging4s.logback

import java.io.ByteArrayOutputStream

import scala.util.Try

import org.slf4j.LoggerFactory
import ch.qos.logback.classic.spi.ILoggingEvent
import ch.qos.logback.core.OutputStreamAppender
import ch.qos.logback.classic.{Level as LogbackLevel, Logger as LogbackLogger}

import logging4s.core.Logging
import logging4s.core.conformance.{BackendConformance, CapturedRecord, JsonRecord}

import LogbackInstances.given

class LogbackConformanceSpec extends BackendConformance:

  LogbackWarmup.touch()

  override def backendName: String = "logback"

  override def capture(loggerName: String)(run: Logging[Try] => Unit): CapturedRecord =
    LoggingLogbackJsonSpec.appenderLock.synchronized {
      val logbackLogger = LoggerFactory.getLogger(loggerName).asInstanceOf[LogbackLogger]
      logbackLogger.setLevel(LogbackLevel.TRACE)

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
      try run(Logging.createTry(loggerName).get)
      finally
        appender.stop()
        logbackLogger.detachAppender(appender)

      JsonRecord.parse(out.toString("UTF-8").linesIterator.next())
    }
