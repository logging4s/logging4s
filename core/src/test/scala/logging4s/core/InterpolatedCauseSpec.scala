package logging4s.core

import org.scalatest.wordspec.AnyWordSpec
import org.scalatest.matchers.should.Matchers

import logging4s.core.syntax.all.*
import logging4s.core.testing.RecordingLogging

class InterpolatedCauseSpec extends AnyWordSpec, Matchers:

  "The logging interpolator" must:
    "pass a throwable hole as the cause, so the backend keeps its stack trace" in:
      val log                 = RecordingLogging[Identity]()
      given Logging[Identity] = log
      val boom                = new IllegalStateException("boom")

      error"payment failed $boom"

      val event = log.recorded.head
      event.cause shouldEqual Some(boom)
      event.keys shouldEqual Nil
      event.message shouldEqual "payment failed"

    "keep the other holes as values next to the cause" in:
      val log                 = RecordingLogging[Identity]()
      given Logging[Identity] = log
      val orderId             = 42
      val boom                = new RuntimeException("boom")

      error"payment failed $orderId $boom"

      val event = log.recorded.head
      event.cause shouldEqual Some(boom)
      event.keys shouldEqual Seq("order_id")

    "use the last throwable hole as the cause and keep the earlier ones as values" in:
      val log                 = RecordingLogging[Identity]()
      given Logging[Identity] = log
      val first               = new RuntimeException("first")
      val second              = new IllegalArgumentException("second")

      warn"retry failed $first then $second"

      val event = log.recorded.head
      event.cause shouldEqual Some(second)
      event.keys shouldEqual Seq("first")

    "leave the cause empty when no hole is a throwable" in:
      val log                 = RecordingLogging[Identity]()
      given Logging[Identity] = log
      val attempts            = 3

      info"retried $attempts"

      log.recorded.head.cause shouldEqual None
