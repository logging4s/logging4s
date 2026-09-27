package logging4s.console

import java.io.{ByteArrayOutputStream, PrintStream}

import scala.util.Try

import logging4s.core.Logging
import logging4s.core.config.LoggableEncodingConfig
import logging4s.core.conformance.{BackendConformance, CapturedRecord, JsonRecord}

import ConsoleInstances.given

class ConsoleConformanceSpec extends BackendConformance:

  override def backendName: String = "console"

  private given ConsoleConfig          = ConsoleConfig(Threshold.At(logging4s.core.Level.Trace), Format.Json, ColorMode.Off, Stream.Stdout, -1)
  private given LoggableEncodingConfig = LoggableEncodingConfig.Default

  override def capture(loggerName: String)(run: Logging[Try] => Unit): CapturedRecord =
    ConsoleCaptureLock.synchronized {
      val out      = new ByteArrayOutputStream()
      val original = System.out

      System.setOut(new PrintStream(out, true, "UTF-8"))
      try run(Logging.createTry(loggerName).get)
      finally System.setOut(original)

      val line = out
        .toString("UTF-8")
        .linesIterator
        .find(candidate => candidate.startsWith("{") && candidate.contains(loggerName))
        .getOrElse(throw new IllegalStateException(s"no console record for $loggerName"))

      JsonRecord.parse(line)
    }
