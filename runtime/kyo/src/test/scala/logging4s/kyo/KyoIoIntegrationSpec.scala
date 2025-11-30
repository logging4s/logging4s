package logging4s.kyo

import scala.concurrent.{ExecutionContext, Future}

import org.scalatest.wordspec.AsyncWordSpec
import org.scalatest.matchers.should.Matchers
import org.scalatest.Assertion

import kyo.*
import kyo.kernel.Platform

import logging4s.core.{Delay, Logging}

import instances.given
import AllowUnsafe.embrace.given

class KyoIoIntegrationSpec extends AsyncWordSpec with Matchers:

  override val executionContext: ExecutionContext = Platform.executionContext
  given ExecutionContext = executionContext

  "Kyo integration" must:
    "use given instance with IO implementation for Delay" in runKyo:
      val expected = "test_value"
      Delay[KIO].delay(expected).map(_ shouldEqual expected)

    "right create logging instance for IO monad" in runKyo:
      Logging.create[KIO]("test").map(_ => succeed)

  def runKyo(v: Assertion < IO): Future[Assertion] =
    Future(IO.Unsafe.evalOrThrow(v))
