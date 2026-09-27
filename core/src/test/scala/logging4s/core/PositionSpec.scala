package logging4s.core

import org.scalatest.wordspec.AnyWordSpec
import org.scalatest.matchers.should.Matchers

import logging4s.core.syntax.all.*
import logging4s.core.testing.RecordingLogging

class PositionSpec extends AnyWordSpec, Matchers:

  private def opaqueWrapper(log: Logging[Identity], message: String): Unit =
    log.info(message)

  private def propagatingWrapper(log: Logging[Identity], message: String)(using Position): Unit =
    log.info(message)

  "The captured position" must:
    "point at the call site in the caller's own file" in:
      val log = RecordingLogging[Identity]()

      log.info("direct")
      val here = summon[Position]

      log.recorded.head.position.file shouldEqual here.file
      log.recorded.head.position.line shouldEqual here.line - 1

    "stop at a wrapper that does not propagate Position" in:
      val log = RecordingLogging[Identity]()

      opaqueWrapper(log, "wrapped")
      val here = summon[Position]

      log.recorded.head.position.line should not equal here.line - 1

    "follow the caller through a wrapper that propagates Position" in:
      val log = RecordingLogging[Identity]()

      propagatingWrapper(log, "wrapped")
      val here = summon[Position]

      log.recorded.head.position.line shouldEqual here.line - 1

    "come from the interpolator's own expansion site" in:
      val log                 = RecordingLogging[Identity]()
      given Logging[Identity] = log

      val count = 7
      info"seen $count"
      val here  = summon[Position]

      log.recorded.head.position.line shouldEqual here.line - 1
