package logging4s.logback

import scala.language.implicitConversions

import net.logstash.logback.marker.Markers.*
import net.logstash.logback.marker.LogstashMarker

import logging4s.core.LoggableValue

private[logback] object MarkerHelper:

  def fromLoggable(value: LoggableValue): LogstashMarker =
    appendRaw(value.key.value, value.json.value)

  def fromLoggable(values: Seq[LoggableValue]): LogstashMarker =
    val deduplicated = LoggableValue.deduplicateKeys(values)

    val logstash = deduplicated match
      case head +: tail =>
        tail.foldLeft(fromLoggable(head)) { (marker, value) =>
          marker.and[LogstashMarker](fromLoggable(value))
        }
      case _            => empty()

    logstash.and[LogstashMarker](LoggableValuesMarker(deduplicated))
