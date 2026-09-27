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

    "strip the quotes zio adds around a string, and nothing else" in:
      import logging4s.core.PlainEncoder

      PlainEncoder[String].encode("test_value").value shouldEqual "test_value"
      PlainEncoder[String].encode("").value shouldEqual ""

    "keep numeric and boolean renderings intact" in:
      import logging4s.core.PlainEncoder

      PlainEncoder[Int].encode(123).value shouldEqual "123"
      PlainEncoder[Int].encode(1).value shouldEqual "1"
      PlainEncoder[Long].encode(-5L).value shouldEqual "-5L"
      PlainEncoder[Boolean].encode(true).value shouldEqual "true"

    "render a char without its quotes" in:
      import logging4s.core.PlainEncoder

      PlainEncoder[Char].encode('a').value shouldEqual "a"
