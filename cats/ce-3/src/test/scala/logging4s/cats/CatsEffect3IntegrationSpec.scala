package logging4s.cats

import org.scalatest.wordspec.AsyncWordSpec
import org.scalatest.matchers.should.Matchers
import cats.effect.testing.scalatest.AsyncIOSpec
import cats.effect.IO
import logging4s.core.{Delay, Logging}

class CatsEffect3IntegrationSpec extends AsyncWordSpec with AsyncIOSpec with Matchers:

  "Cats-effect 3 integration" must {
    import SyncToDelayInstance.given

    "use given instance with Sync implementation for Delay" in {
      val expected = "test_value"

      def delay[F[*]: Delay]: F[String] =
        Delay[F].delay(expected)

      delay[IO].map(_ shouldEqual expected)
    }

    "right create logging instance for IO monad" in {
      for _ <- Logging.create[IO]("test")
      yield assert(true)
    }

  }
