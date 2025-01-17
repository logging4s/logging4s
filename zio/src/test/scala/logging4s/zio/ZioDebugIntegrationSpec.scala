package logging4s.zio

import org.scalatest.matchers.should.Matchers
import org.scalatest.wordspec.AnyWordSpec

import logging4s.core.{JsonEncoder, Loggable}

import instances.given

class ZioDebugIntegrationSpec extends AnyWordSpec with Matchers:

  "Zio prelude integration" must:
    "use given instance with Debug implementation for PlainEncoder" in:
      given JsonEncoder[String] = s => s"\"$s\""

      val expected = "test_value"
      Loggable.make[String]("value").plain(expected) shouldEqual expected
