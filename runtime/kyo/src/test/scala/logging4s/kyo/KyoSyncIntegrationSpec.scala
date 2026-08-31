package logging4s.kyo

import org.scalatest.wordspec.AnyWordSpec
import org.scalatest.matchers.should.Matchers

import kyo.{AllowUnsafe, Sync}

import logging4s.core.{Delay, Logging}

import KyoInstances.given
import logging4s.logback.LogbackInstances.given

class KyoSyncIntegrationSpec extends AnyWordSpec, Matchers:

  private given AllowUnsafe = AllowUnsafe.embrace.danger

  private def run[A](io: KIO[A]): A = Sync.Unsafe.evalOrThrow(io)

  "Kyo integration" must:
    "use the given Delay instance backed by Sync" in:
      run(Delay[KIO].delay("test_value")) shouldEqual "test_value"

    "suspend the thunk instead of running it at construction time" in:
      var evaluations = 0

      val io = Delay[KIO].delay:
        evaluations += 1
        evaluations

      evaluations shouldEqual 0
      run(io) shouldEqual 1
      evaluations shouldEqual 1

    "right create a logging instance for the Sync effect" in:
      run(Logging.create[KIO]("test")) should not be null
