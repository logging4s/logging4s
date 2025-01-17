package logging4s.kyo

import scala.concurrent.{ExecutionContext, Future}

import org.scalatest.NonImplicitAssertions
import org.scalatest.wordspec.AsyncWordSpec
import org.scalatest.matchers.should.Matchers

import kyo.*
import kyo.kernel.Platform
import kyo.internal.BaseKyoTest

import logging4s.core.{Delay, Logging}

import instances.given
import AllowUnsafe.embrace.given

class KyoIoIntegrationSpec extends AsyncWordSpec with Matchers with BaseKyoTest[IO] with NonImplicitAssertions:

  override def executionContext: ExecutionContext = Platform.executionContext

  type Assertion = org.scalatest.Assertion
  def success: Assertion = succeed

  "Kyo integration" must:
    "use given instance with IO implementation for Delay" in run:
      val expected = "test_value"
      Delay[KIO].delay(expected).map(_ shouldEqual expected)

    "right create logging instance for IO monad" in run:
      Logging.create[KIO]("test").map(_ => succeed)

  override def run(v: Future[Assertion] < IO): Future[Assertion] =
    IO.Unsafe.evalOrThrow(v)
