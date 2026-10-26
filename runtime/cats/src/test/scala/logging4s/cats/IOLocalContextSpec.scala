package logging4s.cats

import org.scalatest.wordspec.AnyWordSpec
import org.scalatest.matchers.should.Matchers

import cats.effect.IO
import cats.effect.unsafe.implicits.global

import logging4s.core.Logging
import logging4s.core.syntax.all.*
import logging4s.core.testing.RecordingLogging

import CatsInstances.given

class IOLocalContextSpec extends AnyWordSpec, Matchers:

  "IOLocalContext" must:
    "add the scoped context to the records made inside the scope only" in:
      val base = RecordingLogging[IO]()

      val program =
        for
          local <- IOLocalContext.make
          log    = Logging.contextual(base)(using local)
          _     <- local.scoped("r-1".asLogValue("request_id"))(log.info("inside"))
          _     <- log.info("outside")
        yield ()

      program.unsafeRunSync()

      base.recorded.map(_.keys) shouldEqual Vector(Seq("request_id"), Nil)

    "carry the context into a fiber started inside the scope" in:
      val base = RecordingLogging[IO]()

      val program =
        for
          local <- IOLocalContext.make
          log    = Logging.contextual(base)(using local)
          _     <- local.scoped("r-1".asLogValue("request_id"))(log.info("child").start.flatMap(_.join))
        yield ()

      program.unsafeRunSync()

      base.recorded.map(_.keys) shouldEqual Vector(Seq("request_id"))

    "restore the previous context when the scoped effect fails" in:
      val base = RecordingLogging[IO]()

      val program =
        for
          local <- IOLocalContext.make
          log    = Logging.contextual(base)(using local)
          _     <- local.scoped("r-1".asLogValue("request_id"))(IO.raiseError[Unit](new RuntimeException("boom"))).attempt
          _     <- log.info("after")
        yield ()

      program.unsafeRunSync()

      base.recorded.map(_.keys) shouldEqual Vector(Nil)
