package logging4s.cats

import org.scalatest.matchers.should.Matchers
import org.scalatest.wordspec.AnyWordSpec
import logging4s.core.{JsonEncoder, Loggable}

import logging4s.cats.ShowToPlainEncoderInstance.given

class CatsShowIntegrationSpec extends AnyWordSpec with Matchers:

  "Cats core integration" must:
    "use given instance with Show implementation for PlainEncoder" in:
      given JsonEncoder[String] = s => s"\"$s\""

      val expected = "test_value"
      Loggable.make[String]("value").plain(expected) shouldEqual expected
