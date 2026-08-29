package logging4s.logback

import scala.util.Try

import org.scalatest.matchers.should.Matchers
import org.scalatest.wordspec.AnyWordSpec

import logging4s.core.{LoggableValue, Logging}
import logging4s.core.syntax.all.*

import LogbackInstances.given

class LoggingLogbackSpec extends AnyWordSpec, Matchers:

  LogbackWarmup.touch()

  "Logging backed by logback" must:
    "right create for default Delay implementations" in:
      val resultTry =
        for
          logging <- Logging.createTry("LoggingLogbackSpec")
          _       <- logging.info("Test log")
        yield ()

      assert(resultTry.isSuccess)

    "not fail when a call site passes no values" in:
      val noValues = Seq.empty[LoggableValue]

      val resultTry =
        for
          logging <- Logging.createTry("LoggingLogbackSpec-no-values")
          _       <- logging.info("Test log", noValues*)
        yield ()

      assert(resultTry.isSuccess)

    "not fail for an interpolated message without interpolated values" in:
      given Logging[Try] = Logging.createTry("LoggingLogbackSpec-interpolated").get

      assert(info"application started".isSuccess)
