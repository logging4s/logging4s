package logging4s.slf4j

import org.scalatest.matchers.should.Matchers
import org.scalatest.wordspec.AnyWordSpec

import logging4s.core.{JsonString, Logging, LoggableValue, PlainString, ValueKey}

import Slf4jInstances.given

class LoggingSlf4jSpec extends AnyWordSpec, Matchers:

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
          _       <- logging.info("Test log with values", LoggableValue(ValueKey("user"), PlainString("John"), JsonString("\"John\"")))
        yield ()

      assert(resultTry.isSuccess)

    "right log duplicated keys without throwing" in:
      val resultTry =
        for
          logging <- Logging.createTry("LoggingSlf4jSpec")
          _       <- logging.error(
                       "Test log with error and duplicated keys",
                       new RuntimeException("boom"),
                       LoggableValue(ValueKey("k"), PlainString("1"), JsonString("\"1\"")),
                       LoggableValue(ValueKey("k"), PlainString("2"), JsonString("\"2\"")),
                     )
        yield ()

      assert(resultTry.isSuccess)
