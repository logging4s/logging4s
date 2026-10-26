package logging4s.logback

import tools.jackson.core.JsonGenerator
import net.logstash.logback.marker.LogstashMarker

import logging4s.core.LoggableValue

private[logback] final class LoggableValuesMarker(val values: Seq[LoggableValue]) extends LogstashMarker(LoggableValuesMarker.Name):
  override def writeTo(generator: JsonGenerator): Unit = ()

private[logback] object LoggableValuesMarker:
  private val Name = "logging4s.values"
