package logging4s.cats

import org.scalatest.wordspec.AnyWordSpec
import org.scalatest.matchers.should.Matchers

import logging4s.core.{JsonEncoder, JsonString, Loggable}

import ShowToPlainEncoderInstance.given

class CatsShowIntegrationSpec extends AnyWordSpec, Matchers:

  "Cats core integration" must:
    "use given instance with Show implementation for PlainEncoder" in:
      given JsonEncoder[String] = s => JsonString.quoted(s)

      val expected = "test_value"
      Loggable.make[String]("value").plain(expected) shouldEqual expected
