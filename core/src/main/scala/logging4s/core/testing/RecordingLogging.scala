package logging4s.core.testing

import java.util.concurrent.atomic.AtomicReference

import logging4s.core.{Delay, Level, Logging, LoggableValue, LoggingContext, Position}

final case class RecordedEvent(
    level: Level,
    message: String,
    cause: Option[Throwable],
    values: Seq[LoggableValue],
    context: Seq[LoggableValue],
    position: Position,
):
  def keys: Seq[String] = (context ++ values).map(_.key.value)

  def valueOf(key: String): Option[String] =
    (context ++ values).find(_.key.value == key).map(_.json.value)

final class RecordingLogging[F[*]: Delay] private (
    context: LoggingContext,
    threshold: Level,
    state: AtomicReference[Vector[RecordedEvent]],
) extends Logging[F]:

  override def withContext(moreContext: LoggingContext): Logging[F] =
    new RecordingLogging(context + moreContext, threshold, state)

  override def enabled(level: Level): Boolean = level.enabledAt(threshold)

  override def unit: F[Unit] = Delay[F].unit

  override def emit(level: Level, message: String, cause: Option[Throwable], values: Seq[LoggableValue])(using
      position: Position
  ): F[Unit] =
    Delay[F].delay {
      if enabled(level) then
        val event = RecordedEvent(level, message, cause, values, context.values, position)
        state.updateAndGet(_ :+ event): Unit
    }

  def recorded: Vector[RecordedEvent] = state.get

  def clear(): Unit = state.set(Vector.empty)

object RecordingLogging:

  def apply[F[*]: Delay](threshold: Level = Level.Trace): RecordingLogging[F] =
    new RecordingLogging(LoggingContext.empty, threshold, new AtomicReference(Vector.empty))
