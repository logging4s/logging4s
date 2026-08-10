package logging4s.console

import java.io.{PrintWriter, StringWriter}
import java.time.Instant

import logging4s.core.{LoggableValue, StructuredJson}
import logging4s.core.config.LoggableEncodingConfig
import logging4s.core.syntax.all.plain

private[console] object Renderer:

  private val Esc: Char = 27.toChar

  def render(
      console: ConsoleConfig,
      level: Level,
      logger: String,
      message: String,
      cause: Option[Throwable],
      values: Seq[LoggableValue],
  )(using LoggableEncodingConfig): String =
    console.format match
      case Format.Json  => json(console, level, logger, message, cause, values)
      case Format.Plain => plain(console, level, logger, message, cause, values)

  private def fullMessage(message: String, values: Seq[LoggableValue])(using LoggableEncodingConfig): String =
    if values.isEmpty then message else s"$message: ${values.plain}"

  private def json(
      console: ConsoleConfig,
      level: Level,
      logger: String,
      message: String,
      cause: Option[Throwable],
      values: Seq[LoggableValue],
  )(using LoggableEncodingConfig): String =
    val envelope = Seq(
      "@timestamp" -> Instant.ofEpochMilli(System.currentTimeMillis).toString,
      "level"      -> level.toString.toUpperCase,
      "logger"     -> logger,
      "thread"     -> Thread.currentThread.getName,
      "message"    -> fullMessage(message, values),
    )
    val trace    = cause.map(t => "stack_trace" -> stackTrace(t, console.maxStackTraceLines)).toSeq

    StructuredJson.line(envelope ++ trace, values)

  private def plain(
      console: ConsoleConfig,
      level: Level,
      logger: String,
      message: String,
      cause: Option[Throwable],
      values: Seq[LoggableValue],
  )(using LoggableEncodingConfig): String =
    val timestamp = Instant.ofEpochMilli(System.currentTimeMillis).toString
    val base      = s"$timestamp ${level.toString.toUpperCase} $logger - ${fullMessage(message, values)}"
    val withTrace = cause.fold(base)(t => s"$base${System.lineSeparator}${stackTrace(t, console.maxStackTraceLines)}")

    colorize(console, level, withTrace)

  private def colorize(console: ConsoleConfig, level: Level, text: String): String =
    val enabled = console.color match
      case ColorMode.On   => true
      case ColorMode.Off  => false
      case ColorMode.Auto => System.console() != null

    if !enabled then text else s"$Esc[${colorCode(level)}m$text$Esc[0m"

  private def colorCode(level: Level): String =
    level match
      case Level.Error => "31"
      case Level.Warn  => "33"
      case Level.Info  => "32"
      case Level.Debug => "36"
      case Level.Trace => "90"

  private def stackTrace(throwable: Throwable, maxLines: Int): String =
    val writer = new StringWriter()
    throwable.printStackTrace(new PrintWriter(writer))
    val full   = writer.toString

    if maxLines < 0 then full
    else
      val lines = full.linesIterator.toVector
      if lines.length <= maxLines then full
      else (lines.take(maxLines) :+ s"... ${lines.length - maxLines} more").mkString(System.lineSeparator)
