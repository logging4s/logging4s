package logging4s.core.testing

import java.util.concurrent.atomic.AtomicReference

import logging4s.core.config.LoggableEncodingConfig
import logging4s.core.{Delay, Level, LogRecord, Logging, LoggableValue, LoggingContext, Position}

final case class RecordedEvent(
    level: Level,
    message: String,
    cause: Option[Throwable],
    values: Seq[LoggableValue],
    context: Seq[LoggableValue],
    fields: Seq[LoggableValue],
    position: Position,
):
  def keys: Seq[String] = fields.map(_.key.value)

  def valueOf(key: String): Option[String] =
    fields.find(_.key.value == key).map(_.json.value)

final class RecordingLogging[F[*]: Delay] private (
    context: Seq[LoggableValue],
    threshold: Level,
    state: AtomicReference[Vector[RecordedEvent]],
)(using LoggableEncodingConfig)
    extends Logging[F]:

  override def withContext(moreContext: LoggingContext): Logging[F] =
    new RecordingLogging(LogRecord.context(context, moreContext).values, threshold, state)

  override def enabled(level: Level): Boolean = level.enabledAt(threshold)

  override def unit: F[Unit] = Delay[F].unit

  override def emit(level: Level, message: String, cause: Option[Throwable], values: Seq[LoggableValue])(using
      position: Position
  ): F[Unit] =
    Delay[F].delay {
      if enabled(level) then
        val record = LogRecord.prepare(context, values, position, LogRecord.ReservedKeys)
        val event  = RecordedEvent(level, message, cause, LoggableValue.normalizeKeys(values), context, record.values, position)
        state.updateAndGet(_ :+ event): Unit
    }

  def recorded: Vector[RecordedEvent] = state.get

  def clear(): Unit = state.set(Vector.empty)

object RecordingLogging:

  def apply[F[*]: Delay](threshold: Level = Level.Trace)(using LoggableEncodingConfig): RecordingLogging[F] =
    new RecordingLogging(Seq.empty, threshold, new AtomicReference(Vector.empty))
