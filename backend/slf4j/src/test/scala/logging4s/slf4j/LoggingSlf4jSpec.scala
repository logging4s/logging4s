package logging4s.slf4j

import org.scalatest.matchers.should.Matchers
import org.scalatest.wordspec.AnyWordSpec

import logging4s.core.{Logging, LoggableValue}

import Slf4jInstances.given

class LoggingSlf4jSpec extends AnyWordSpec with Matchers:

  "Logging backed by bare slf4j" must:
    "right create for default Delay implementations" in:
      val resultTry =
        for
          logging <- Logging.createTry("LoggingSlf4jSpec")
          _       <- logging.info("Test log")
        yield ()

      assert(resultTry.isSuccess)

    "right log with structured values" in:
      val resultTry =
        for
          logging <- Logging.createTry("LoggingSlf4jSpec")
          _       <- logging.info("Test log with values", LoggableValue("user", "John", "\"John\""))
        yield ()

      assert(resultTry.isSuccess)

    "right log duplicated keys without throwing" in:
      val resultTry =
        for
          logging <- Logging.createTry("LoggingSlf4jSpec")
          _       <- logging.error(
                       "Test log with error and duplicated keys",
                       new RuntimeException("boom"),
                       LoggableValue("k", "1", "\"1\""),
                       LoggableValue("k", "2", "\"2\""),
                     )
        yield ()

      assert(resultTry.isSuccess)
