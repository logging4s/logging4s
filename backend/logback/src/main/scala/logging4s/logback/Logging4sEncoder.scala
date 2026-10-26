package logging4s.logback

import java.time.Instant
import java.nio.charset.StandardCharsets

import scala.collection.mutable

import org.slf4j.Marker
import ch.qos.logback.core.encoder.EncoderBase
import ch.qos.logback.classic.spi.{ILoggingEvent, ThrowableProxyUtil}

import logging4s.core.{LoggableValue, StructuredJson}

class Logging4sEncoder extends EncoderBase[ILoggingEvent]:

  override def headerBytes(): Array[Byte] = Array.emptyByteArray
  override def footerBytes(): Array[Byte] = Array.emptyByteArray

  override def encode(event: ILoggingEvent): Array[Byte] =
    val builder   = new StructuredJson.Builder
    val values    = loggableValues(event.getMarkerList)
    val mdc       = event.getMDCPropertyMap
    val keyValues = event.getKeyValuePairs
    val throwable = event.getThrowableProxy

    builder.field("@timestamp", Instant.ofEpochMilli(event.getTimeStamp).toString)
    builder.field("level", event.getLevel.toString)
    builder.field("logger", event.getLoggerName)
    builder.field("thread", event.getThreadName)
    builder.field("message", Option(event.getFormattedMessage).getOrElse(""))

    if !mdc.isEmpty || (keyValues != null && !keyValues.isEmpty) then
      val own   = values.iterator.map(_.key.value).toSet
      val taken = mutable.HashSet.from(Logging4sEncoder.Envelope) ++= own

      def ambient(key: String, value: String): Unit =
        if !own.contains(key) then
          var name  = key
          var index = 2

          while !taken.add(name) do
            name = s"${key}_$index"
            index += 1

          builder.field(name, value)

      mdc.forEach((key, value) => ambient(key, value))
      if keyValues != null then keyValues.forEach(pair => ambient(pair.key, String.valueOf(pair.value)))

    if throwable != null then builder.field("stack_trace", ThrowableProxyUtil.asString(throwable))

    builder.values(values)

    (builder.result + "\n").getBytes(StandardCharsets.UTF_8)

  private def loggableValues(markers: java.util.List[Marker]): Seq[LoggableValue] =
    if markers == null || markers.isEmpty
    then Seq.empty
    else
      val collected = mutable.ArrayBuffer.empty[LoggableValue]
      markers.forEach(marker => collect(marker, collected))
      collected.toSeq

  private def collect(marker: Marker, into: mutable.ArrayBuffer[LoggableValue]): Unit =
    marker match
      case values: LoggableValuesMarker => into ++= values.values: Unit
      case _ if marker.hasReferences    => marker.iterator.forEachRemaining(ref => collect(ref, into))
      case _                            => ()

object Logging4sEncoder:
  private val Envelope = Set("@timestamp", "level", "logger", "thread", "message", "stack_trace")
