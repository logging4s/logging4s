package logging4s.core

import org.scalatest.wordspec.AnyWordSpec
import org.scalatest.matchers.should.Matchers

import logging4s.core.syntax.all.*
import logging4s.core.testing.RecordingLogging

class ContextualLoggingSpec extends AnyWordSpec, Matchers:
  import ContextualLoggingSpec.{*, given}

  "Logging.contextual" must:
    "add the ambient context to every record" in:
      val base  = RecordingLogging[Lazy]()
      val local = VarContext()
      val log   = Logging.contextual(base)(using local)

      local.scoped("r-1".asLogValue("request_id"))(log.info("handled"))()

      base.recorded.head.valueOf("request_id") shouldEqual Some("\"r-1\"")

    "let a call-site value win over the ambient context" in:
      val base  = RecordingLogging[Lazy]()
      val local = VarContext()
      val log   = Logging.contextual(base)(using local)

      local.scoped("ambient".asLogValue("user"))(log.info("x", "call".asLogValue("user")))()

      base.recorded.head.valueOf("user") shouldEqual Some("\"call\"")

    "let the ambient context win over the logger's own context, since it is scoped closer to the call" in:
      val base  = RecordingLogging[Lazy]()
      val local = VarContext()
      val log   = Logging.contextual(base)(using local).withContext(LoggingContext("static".asLogValue("user")))

      local.scoped("ambient".asLogValue("user"))(log.info("x"))()

      base.recorded.head.valueOf("user") shouldEqual Some("\"ambient\"")

    "nest scopes, the inner one adding to the outer" in:
      val base  = RecordingLogging[Lazy]()
      val local = VarContext()
      val log   = Logging.contextual(base)(using local)

      local.scoped("r-1".asLogValue("request_id"))(local.scoped("u-1".asLogValue("user"))(log.info("x")))()

      base.recorded.head.keys shouldEqual Seq("request_id", "user")

    "restore the previous context once the scope ends" in:
      val base  = RecordingLogging[Lazy]()
      val local = VarContext()
      val log   = Logging.contextual(base)(using local)

      local.scoped("r-1".asLogValue("request_id"))(log.info("inside"))()
      log.info("outside")()

      base.recorded.map(_.keys) shouldEqual Vector(Seq("request_id"), Nil)

    "not read the local context for a disabled level" in:
      val base  = RecordingLogging[Lazy](Level.Info)
      val local = VarContext()
      val log   = Logging.contextual(base)(using local)

      log.debug("noise")()

      local.reads shouldEqual 0
      base.recorded shouldBe empty

object ContextualLoggingSpec:
  type Lazy[A] = () => A

  given Delay[Lazy] = new:
    override def delay[A](a: => A): Lazy[A] = () => a

  final class VarContext extends LocalContext[Lazy]:
    private var current = LoggingContext.empty
    var reads           = 0

    override def use[A](f: LoggingContext => Lazy[A]): Lazy[A] =
      () =>
        reads += 1
        f(current)()

    override def scoped[A](context: LoggingContext)(fa: Lazy[A]): Lazy[A] =
      () =>
        val previous = current
        current = current + context
        try fa()
        finally current = previous
