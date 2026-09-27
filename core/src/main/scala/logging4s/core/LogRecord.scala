package logging4s.core

import logging4s.core.config.LoggableEncodingConfig

final case class LogRecord(values: Seq[LoggableValue], structured: Seq[LoggableValue])

object LogRecord:

  val ReservedKeys: Set[ValueKey] =
    Set("@timestamp", "timestamp", "level", "logger", "logger_name", "thread", "thread_name", "message", "stack_trace")
      .map(ValueKey.apply)

  def context(current: Seq[LoggableValue], added: LoggingContext)(using LoggableEncodingConfig): LoggingContext =
    LoggingContext(LoggableValue.mergeByKey(current, LoggableValue.normalizeKeys(added.values)))

  def prepare(
      context: Seq[LoggableValue],
      values: Seq[LoggableValue],
      position: Position,
      reserved: Set[ValueKey] = Set.empty,
  )(using LoggableEncodingConfig): LogRecord =
    val merged  = LoggableValue.mergeByKey(context, LoggableValue.normalizeKeys(values))
    val deduped = LoggableValue.deduplicateKeys(merged, reserved)

    LogRecord(deduped, LoggableValue.withSource(deduped, position))
