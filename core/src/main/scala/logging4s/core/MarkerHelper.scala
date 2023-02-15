package logging4s.core

import scala.language.implicitConversions

import net.logstash.logback.marker.Markers.*
import net.logstash.logback.marker.LogstashMarker

object MarkerHelper:

  def fromLoggable(value: LoggableValue): LogstashMarker =
    appendRaw(value.key, value.json)

  def fromLoggable(values: Seq[LoggableValue]): LogstashMarker =
    val groupedByDuplicatedKeys = values.groupBy(_.key)

    val deduplicated =
      if groupedByDuplicatedKeys.size == values.size
      then values
      else
        groupedByDuplicatedKeys.flatMap { case (key, duplicates) =>
          if duplicates.size == 1
          then duplicates
          else
            duplicates.zipWithIndex.map { case (value, index) =>
              value.copy(key = s"${key}_${index + 1}")
            }
        }

    deduplicated.tail.foldLeft(fromLoggable(deduplicated.head)) { (marker, value) =>
      marker.and[LogstashMarker](fromLoggable(value))
    }
