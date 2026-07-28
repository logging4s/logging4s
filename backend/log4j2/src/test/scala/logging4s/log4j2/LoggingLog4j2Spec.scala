package logging4s.log4j2

import org.scalatest.matchers.should.Matchers
import org.scalatest.wordspec.AnyWordSpec

import logging4s.core.{Logging, LoggableValue}

import Log4j2Instances.given

class LoggingLog4j2Spec extends AnyWordSpec with Matchers:

  Log4j2Warmup.touch()

  "Logging backed by log4j2" must:
    "right create for default Delay implementations" in:
      val resultTry =
        for
          logging <- Logging.createTry("LoggingLog4j2Spec")
          _       <- logging.info("Test log")
        yield ()

      assert(resultTry.isSuccess)

    "right log with structured values" in:
      val resultTry =
        for
          logging <- Logging.createTry("LoggingLog4j2Spec")
          _       <- logging.info("Test log with values", LoggableValue("user", "John", "\"John\""))
        yield ()

      assert(resultTry.isSuccess)
