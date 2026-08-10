package logging4s.zio

import org.scalatest.wordspec.AnyWordSpec
import org.scalatest.matchers.should.Matchers

import logging4s.core.{JsonEncoder, JsonString, Loggable}

import ZioInstances.given

class ZioDebugIntegrationSpec extends AnyWordSpec, Matchers:

  "Zio prelude integration" must:
    "use given instance with Debug implementation for PlainEncoder" in:
      given JsonEncoder[String] = s => JsonString.quoted(s)

      val expected = "test_value"
      Loggable.fromEncoders[String]("value").plain(expected) shouldEqual expected
