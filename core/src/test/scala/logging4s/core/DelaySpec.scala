package logging4s.core

import scala.util.{Failure, Success, Try}

import org.scalatest.wordspec.AnyWordSpec
import org.scalatest.matchers.should.Matchers

class DelaySpec extends AnyWordSpec, Matchers:

  "Delay[ThrowableEither]" must:
    "evaluate the thunk exactly once" in:
      var evaluations = 0

      val result = Delay[ThrowableEither].delay:
        evaluations += 1
        evaluations

      evaluations shouldEqual 1
      result shouldEqual Right(1)

    "capture a non fatal failure as Left" in:
      val boom = new RuntimeException("boom")

      Delay[ThrowableEither].delay(throw boom) shouldEqual Left(boom)

    "let a fatal error through instead of capturing it" in:
      an[StackOverflowError] should be thrownBy Delay[ThrowableEither].delay(throw new StackOverflowError)

  "Delay[Try]" must:
    "evaluate the thunk exactly once" in:
      var evaluations = 0

      val result = Delay[Try].delay:
        evaluations += 1
        evaluations

      evaluations shouldEqual 1
      result shouldEqual Success(1)

    "capture a non fatal failure as Failure" in:
      val boom = new RuntimeException("boom")

      Delay[Try].delay(throw boom) shouldEqual Failure(boom)

  "Delay[Identity]" must:
    "evaluate the thunk exactly once" in:
      var evaluations = 0

      val result = Delay[Identity].delay:
        evaluations += 1
        evaluations

      evaluations shouldEqual 1
      result shouldEqual 1
