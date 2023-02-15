package logging4s.cats

import org.scalatest.matchers.should.Matchers
import org.scalatest.wordspec.{AnyWordSpec, AsyncWordSpec}
import cats.effect.IO
import cats.effect.testing.scalatest.AsyncIOSpec
import logging4s.core.Delay

class CatsEffect2IntegrationSpec extends AsyncWordSpec with AsyncIOSpec with Matchers:

  "Cats-effect 2 integration" must {
    import instances.given

    "use given instance with Sync implementation for implement Delay" in {
      val expected = "test_value"

      def delay[F[*]: Delay]: F[String] =
        Delay[F].delay(expected)

      delay[IO].map(_ shouldEqual expected)
    }

    "right create logging instance for IO monad" in {
      for _ <- LoggingCats.create[IO]("test")
      yield assert(true)
    }

  }
