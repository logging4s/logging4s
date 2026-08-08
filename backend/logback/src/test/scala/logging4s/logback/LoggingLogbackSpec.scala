package logging4s.logback

import org.scalatest.matchers.should.Matchers
import org.scalatest.wordspec.AnyWordSpec

import logging4s.core.Logging

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
