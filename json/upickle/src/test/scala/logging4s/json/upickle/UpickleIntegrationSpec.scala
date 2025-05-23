package logging4s.json.upickle

import org.scalatest.matchers.should.Matchers
import org.scalatest.wordspec.AnyWordSpec

import upickle.default.{Writer, macroW}

import logging4s.core.{Loggable, PlainEncoder}

import instances.given

class UpickleIntegrationSpec extends AnyWordSpec with Matchers:

  final case class User(name: String, age: Int)

  given PlainEncoder[User] = user => s"name=${user.name}, age=${user.age}"
  given Writer[User]       = macroW

  "Upickle integration" must:
    "use given instance with Writer implementation for JsonEncoder" in:
      val user     = User("John", 18)
      val expected = """{"name":"John","age":18}"""

      Loggable.make[User]("user").json(user) shouldEqual expected
