package logging4s.cats

import org.scalatest.matchers.should.Matchers
import org.scalatest.wordspec.AnyWordSpec
import logging4s.core.{JsonEncoder, Loggable}

class CatsShowIntegrationSpec extends AnyWordSpec with Matchers:

  "Cats core integration" must {

    "use given instance with Show implementation for PlainEncoder" in {
      import logging4s.cats.ShowToPlainEncoderInstance.given

      val expected = "test_value"

      given JsonEncoder[String] = s => s"\"$s\""

      Loggable.make[String]("value").plain(expected) shouldEqual expected
    }

  }