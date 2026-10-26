package logging4s.kyo

import org.scalatest.wordspec.AnyWordSpec
import org.scalatest.matchers.should.Matchers

import kyo.{AllowUnsafe, Sync}

import logging4s.core.Logging
import logging4s.core.syntax.all.*
import logging4s.core.testing.RecordingLogging

import KyoInstances.given

class KyoLoggingContextSpec extends AnyWordSpec, Matchers:

  private given AllowUnsafe = AllowUnsafe.embrace.danger

  private def run[A](io: KIO[A]): A = Sync.Unsafe.evalOrThrow(io)

  "KyoLoggingContext" must:
    "add the scoped context to the records made inside the scope only" in:
      val base = RecordingLogging[KIO]()
      val log  = Logging.contextual(base)

      val program =
        for
          _ <- KyoLoggingContext.scoped("r-1".asLogValue("request_id"))(log.info("inside"))
          _ <- log.info("outside")
        yield ()

      run(program)

      base.recorded.map(_.keys) shouldEqual Vector(Seq("request_id"), Nil)

    "nest scopes, the inner one adding to the outer" in:
      val base = RecordingLogging[KIO]()
      val log  = Logging.contextual(base)

      run(KyoLoggingContext.scoped("r-1".asLogValue("request_id"))(KyoLoggingContext.scoped("u-1".asLogValue("user"))(log.info("x"))))

      base.recorded.map(_.keys) shouldEqual Vector(Seq("request_id", "user"))

    "expose the current context" in:
      run(KyoLoggingContext.scoped("r-1".asLogValue("request_id"))(KyoLoggingContext.get)).values.map(_.key.value) shouldEqual Seq("request_id")
