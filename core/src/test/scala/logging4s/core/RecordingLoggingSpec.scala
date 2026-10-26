package logging4s.core

import org.scalatest.wordspec.AnyWordSpec
import org.scalatest.matchers.should.Matchers

import logging4s.core.syntax.all.*
import logging4s.core.testing.RecordingLogging

class RecordingLoggingSpec extends AnyWordSpec, Matchers:

  "RecordingLogging" must:
    "let a call-site value override a context value, as every backend does" in:
      val log = RecordingLogging[Identity]()

      log.withContext(LoggingContext("ctx".asLogValue("user"))).info("x", "call".asLogValue("user"))

      val event = log.recorded.head
      event.keys shouldEqual Seq("user")
      event.valueOf("user") shouldEqual Some("\"call\"")

    "normalize keys the way the backends do" in:
      val log                 = RecordingLogging[Identity]()
      given Logging[Identity] = log
      val userId              = 7

      info"loaded $userId"

      log.recorded.head.keys shouldEqual Seq("user_id")

    "suffix a value named after an envelope field" in:
      val log = RecordingLogging[Identity]()

      log.info("x", "warn".asLogValue("level"))

      log.recorded.head.keys shouldEqual Seq("level_2")

    "keep the call site out of the fields and in the position" in:
      val log = RecordingLogging[Identity]()

      log.info("x")

      log.recorded.head.keys should not contain "source"
      log.recorded.head.position.file shouldEqual "RecordingLoggingSpec.scala"

    "keep the call-site values and the context apart from the merged fields" in:
      val log = RecordingLogging[Identity]()

      log.withContext(LoggingContext("svc".asLogValue("service"))).info("x", 1.asLogValue("n"))

      val event = log.recorded.head
      event.values.map(_.key.value) shouldEqual Seq("n")
      event.context.map(_.key.value) shouldEqual Seq("service")
      event.keys shouldEqual Seq("service", "n")
