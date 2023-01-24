package logging4s.core

import scala.language.implicitConversions

import net.logstash.logback.marker.Markers.*
import net.logstash.logback.marker.LogstashMarker

object MarkerHelper:

  def fromLoggable(value: LoggableValue): LogstashMarker =
    appendRaw(value.key, value.json)

  def fromLoggable(values: Seq[LoggableValue]): LogstashMarker =
    values.tail.foldLeft(fromLoggable(values.head)) { (marker, value) =>
      marker.and[LogstashMarker](fromLoggable(value))
    }
