package logging4s.core

import org.scalatest.wordspec.AnyWordSpec
import org.scalatest.matchers.should.Matchers

import logging4s.core.syntax.all.*
import logging4s.core.testing.RecordingLogging

class DynamicLevelSpec extends AnyWordSpec, Matchers:

  private val log: Logging[Identity] = Logging.noop[Identity]

  "Logging.log" must:
    "emit at a level chosen at runtime" in:
      val recording = RecordingLogging[Identity]()
      val level     = Level.parse("warn").getOrElse(Level.Info)

      recording.log(level, "disk almost full", 91.asLogValue("percent"))

      val event = recording.recorded.head
      event.level shouldEqual Level.Warn
      event.message shouldEqual "disk almost full"
      event.keys shouldEqual Seq("percent")

    "carry a cause" in:
      val recording = RecordingLogging[Identity]()
      val boom      = new RuntimeException("boom")

      recording.log(Level.Error, "failed", boom, 1.asLogValue("attempt"))

      recording.recorded.head.cause shouldEqual Some(boom)

    "respect the threshold" in:
      val recording = RecordingLogging[Identity](Level.Info)

      recording.log(Level.Debug, "noise")

      recording.recorded shouldBe empty

    "reject an interpolated message, like the per-level methods" in:
      log.log(Level.Info, "user created") shouldEqual ()
      assertDoesNotCompile("""val id = 1; log.log(Level.Info, s"user $id created")""")
