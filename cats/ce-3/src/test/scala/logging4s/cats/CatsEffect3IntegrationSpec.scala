package logging4s.cats

import org.scalatest.wordspec.AsyncWordSpec
import org.scalatest.matchers.should.Matchers
import cats.effect.testing.scalatest.AsyncIOSpec
import cats.effect.IO
import logging4s.core.Delay

import SyncToDelayInstance.given

class CatsEffect3IntegrationSpec extends AsyncWordSpec with AsyncIOSpec with Matchers:

  "Cats-effect 3 integration" must:
    "use given instance with Sync implementation for Delay" in:
      val expected = "test_value"
      Delay[IO].delay(expected).map(_ shouldEqual expected)

    "right create logging instance for IO monad" in:
      for _ <- LoggingCats.create[IO]("test")
      yield assert(true)
