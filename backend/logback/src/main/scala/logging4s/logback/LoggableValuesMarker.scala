package logging4s.logback

import java.util.Collections

import org.slf4j.Marker

import logging4s.core.LoggableValue

private[logback] final class LoggableValuesMarker(val values: Seq[LoggableValue]) extends Marker:
  override def getName: String                      = LoggableValuesMarker.Name
  override def add(reference: Marker): Unit         = ()
  override def remove(reference: Marker): Boolean   = false
  override def hasChildren: Boolean                 = false
  override def hasReferences: Boolean               = false
  override def iterator: java.util.Iterator[Marker] = Collections.emptyIterator()
  override def contains(other: Marker): Boolean     = false
  override def contains(name: String): Boolean      = false

private[logback] object LoggableValuesMarker:
  private val Name = "logging4s.values"
