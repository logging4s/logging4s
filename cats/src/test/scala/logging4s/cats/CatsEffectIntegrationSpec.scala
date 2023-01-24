package logging4s.cats

import cats.effect.testing.scalatest.AsyncIOSpec
import org.scalatest.matchers.should.Matchers
import org.scalatest.wordspec.AsyncWordSpec

import cats.effect.IO

import logging4s.core.Delay

class CatsEffectIntegrationSpec extends AsyncWordSpec with AsyncIOSpec with Matchers:

  "Cats-effect integration" must {

    "use given instance with Sync implementation for Delay" in {
      import SyncToDelayInstance.given

      val expected = "test_value"

      def delay[F[*]: Delay]: F[String] =
        Delay[F].delay(expected)

      delay[IO].map(_ shouldEqual expected)
    }

  }
