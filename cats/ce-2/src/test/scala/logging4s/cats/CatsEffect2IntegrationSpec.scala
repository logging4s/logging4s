package logging4s.cats

import org.scalatest.matchers.should.Matchers
import org.scalatest.wordspec.{AnyWordSpec, AsyncWordSpec}
import cats.effect.IO
import cats.effect.testing.scalatest.AsyncIOSpec
import logging4s.core.Delay

import instances.given

class CatsEffect2IntegrationSpec extends AsyncWordSpec with AsyncIOSpec with Matchers:

  "Cats-effect 2 integration" must:

    "use given instance with Sync implementation for implement Delay" in:
      val expected = "test_value"
      Delay[IO].delay(expected).map(_ shouldEqual expected)

    "right create logging instance for IO monad" in:
      for _ <- LoggingCats.create[IO]("test")
      yield assert(true)
