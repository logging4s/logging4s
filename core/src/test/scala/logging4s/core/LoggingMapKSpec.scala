package logging4s.core

import scala.util.Try

import org.scalatest.wordspec.AnyWordSpec
import org.scalatest.matchers.should.Matchers

class LoggingMapKSpec extends AnyWordSpec, Matchers:

  final class Recording extends Logging[Identity]:
    var emitted: List[(Level, String)] = Nil

    def withContext(context: LoggingContext): Logging[Identity] = this
    def enabled(level: Level): Boolean                          = true
    def unit: Unit                                              = ()

    def emit(level: Level, message: String, cause: Option[Throwable], values: Seq[LoggableValue]): Unit =
      emitted = emitted :+ (level, message)

  "Logging.mapK" must:
    "route every level through the natural transformation" in:
      val recording = new Recording
      val lifted    = recording.mapK[Try]([A] => (a: Identity[A]) => Try(a))

      lifted.info("hello").isSuccess shouldEqual true
      lifted.error("boom").isSuccess shouldEqual true

      recording.emitted shouldEqual List(Level.Info -> "hello", Level.Error -> "boom")

    "keep the level check of the underlying logger" in:
      val recording = new Recording
      val lifted    = recording.mapK[Try]([A] => (a: Identity[A]) => Try(a))

      lifted.enabled(Level.Trace) shouldEqual true

    "survive withContext" in:
      val recording = new Recording
      val lifted    = recording.mapK[Try]([A] => (a: Identity[A]) => Try(a))

      lifted.withContext(LoggingContext.empty).info("scoped").isSuccess shouldEqual true

      recording.emitted shouldEqual List(Level.Info -> "scoped")

    "lift unit as well" in:
      val recording = new Recording
      val lifted    = recording.mapK[Try]([A] => (a: Identity[A]) => Try(a))

      lifted.unit.isSuccess shouldEqual true
