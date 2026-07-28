package logging4s.rapid

import org.scalatest.wordspec.AsyncWordSpec
import org.scalatest.matchers.should.Matchers

import rapid.{AsyncTaskSpec, Task}

import logging4s.core.{Delay, Logging}
import logging4s.logback.LogbackInstances.given

import logging4s.rapid.RapidInstances.given

class RapidTaskIntegrationSpec extends AsyncWordSpec with AsyncTaskSpec with Matchers:

  "Rapid integration" must:
    "use given instance with Sync implementation for Delay" in:
      val expected = "test_value"
      Delay[Task].delay(expected).map(_ shouldEqual expected)

    "right create logging instance for IO monad" in:
      for _ <- Logging.create[Task]("test")
      yield assert(true)
