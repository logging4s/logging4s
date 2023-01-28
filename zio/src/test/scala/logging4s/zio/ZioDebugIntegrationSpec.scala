package logging4s.zio

import org.scalatest.matchers.should.Matchers
import org.scalatest.wordspec.AnyWordSpec
import logging4s.core.{JsonEncoder, Loggable}

class ZioDebugIntegrationSpec extends AnyWordSpec with Matchers:

  "Zio prelude integration" must {

    "use given instance with Debug implementation for PlainEncoder" in {
      import DebugToPlainEncoderInstance.given

      val expected = "test_value"

      given JsonEncoder[String] = s => s"\"$s\""

      Loggable.make[String]("value").plain(expected) shouldEqual expected
    }

  }
