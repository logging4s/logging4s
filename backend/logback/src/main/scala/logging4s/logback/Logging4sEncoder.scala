package logging4s.logback

import java.nio.charset.StandardCharsets
import java.time.Instant

import org.slf4j.Marker

import ch.qos.logback.classic.spi.{ILoggingEvent, ThrowableProxyUtil}
import ch.qos.logback.core.encoder.EncoderBase

import logging4s.core.StructuredJson

class Logging4sEncoder extends EncoderBase[ILoggingEvent]:

  override def headerBytes(): Array[Byte] = Array.emptyByteArray
  override def footerBytes(): Array[Byte] = Array.emptyByteArray

  override def encode(event: ILoggingEvent): Array[Byte] =
    val builder = new StructuredJson.Builder

    builder.field("@timestamp", Instant.ofEpochMilli(event.getTimeStamp).toString)
    builder.field("level", event.getLevel.toString)
    builder.field("logger", event.getLoggerName)
    builder.field("thread", event.getThreadName)
    builder.field("message", Option(event.getFormattedMessage).getOrElse(""))

    event.getMDCPropertyMap.forEach((key, value) => builder.field(key, value))

    val throwable = event.getThrowableProxy
    if throwable != null then builder.field("stack_trace", ThrowableProxyUtil.asString(throwable))

    val markers = event.getMarkerList
    if markers != null then markers.forEach(marker => appendValues(marker, builder))

    (builder.result + "\n").getBytes(StandardCharsets.UTF_8)

  private def appendValues(marker: Marker, builder: StructuredJson.Builder): Unit =
    marker match
      case values: LoggableValuesMarker => builder.values(values.values)
      case _ if marker.hasReferences    => marker.iterator.forEachRemaining(ref => appendValues(ref, builder))
      case _                            => ()
