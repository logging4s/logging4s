package logging4s.zio

import org.scalatest.matchers.should.Matchers
import org.scalatest.wordspec.AnyWordSpec

import zio.{Runtime, Task, Unsafe}

import logging4s.core.{Delay, Logging}

import instances.given

class ZioTaskIntegrationSpec extends AnyWordSpec with Matchers:

  "Zio integration" must {
    val runtime = Runtime.default

    "use given instance with Task for implement Delay" in {
      val expected = "test_value"

      val delayTask: Task[String] =
        for value <- Delay[Task].delay(expected)
        yield value

      Unsafe.unsafe { case given Unsafe =>
        runtime.unsafe.run(delayTask).getOrThrowFiberFailure() shouldEqual expected
      }
    }

    "right create logging instance for Task" in {
      for _ <- Logging.create[Task]("test")
      yield assert(true)
    }

  }
