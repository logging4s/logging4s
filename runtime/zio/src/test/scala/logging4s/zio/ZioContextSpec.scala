package logging4s.zio

import org.scalatest.wordspec.AnyWordSpec
import org.scalatest.matchers.should.Matchers

import zio.{NonEmptyChunk, Runtime, Task, UIO, Unsafe, ZEnvironment, ZIO}

import logging4s.core.syntax.all.*
import logging4s.core.{Delay, Loggable, Logging}
import logging4s.core.testing.RecordingLogging

import logging4s.logback.LogbackInstances.given
import ZioInstances.given

class ZioContextSpec extends AnyWordSpec, Matchers:

  private val runtime = Runtime.default

  private def run[A](zio: ZIO[Any, Throwable, A]): A =
    Unsafe.unsafe { case given Unsafe =>
      runtime.unsafe.run(zio).getOrThrowFiberFailure()
    }

  "Zio data instances" must:
    "render a NonEmptyChunk like a collection" in:
      Loggable[NonEmptyChunk[Int]].key shouldEqual "ints"
      Loggable[NonEmptyChunk[Int]].json(NonEmptyChunk(1, 2, 3)) shouldEqual "[1,2,3]"

  "Delay[UIO]" must:
    "suspend the thunk and run it in UIO" in:
      var evaluations = 0

      val effect = Delay[UIO].delay {
        evaluations += 1
        evaluations
      }

      evaluations shouldEqual 0
      run(effect) shouldEqual 1

    "create a logging instance for UIO next to the Task one" in:
      run(Logging.create[UIO]("test")) should not be null
      run(Logging.create[Task]("test")) should not be null

  "ZioLoggingContext" must:
    "add the scoped context to the records made inside the scope only" in:
      val base = RecordingLogging[Task]()
      val log  = Logging.contextual(base)

      run(ZioLoggingContext.scoped("r-1".asLogValue("request_id"))(log.info("inside")) *> log.info("outside"))

      base.recorded.map(_.keys) shouldEqual Vector(Seq("request_id"), Nil)

    "carry the context into a fiber forked inside the scope, and not leak the child's back" in:
      val base = RecordingLogging[Task]()
      val log  = Logging.contextual(base)

      val child   = ZioLoggingContext.scoped("c".asLogValue("child"))(log.info("child"))
      val program = ZioLoggingContext.scoped("r-1".asLogValue("request_id"))(child.fork.flatMap(_.join) *> log.info("parent"))

      run(program)

      base.recorded.map(_.keys) shouldEqual Vector(Seq("request_id", "child"), Seq("request_id"))

    "scope an effect with an environment and a typed error" in:
      val base = RecordingLogging[UIO]()
      val log  = Logging.contextual(base)

      val effect: ZIO[Int, String, Unit] =
        ZIO.serviceWithZIO[Int](n => log.info("loaded", n.asLogValue("n")))

      run(ZioLoggingContext.scoped("r-1".asLogValue("request_id"))(effect).provideEnvironment(ZEnvironment(5)).mapError(new RuntimeException(_)))

      base.recorded.map(_.keys) shouldEqual Vector(Seq("request_id", "n"))
