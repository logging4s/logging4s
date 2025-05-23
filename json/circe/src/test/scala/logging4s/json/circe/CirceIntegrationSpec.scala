package logging4s.json.circe

import org.scalatest.matchers.should.Matchers
import org.scalatest.wordspec.AnyWordSpec

import io.circe.Encoder
import io.circe.generic.semiauto.deriveEncoder

import logging4s.core.{Loggable, PlainEncoder}

import instances.given

class CirceIntegrationSpec extends AnyWordSpec with Matchers:

  final case class User(name: String, age: Int)

  given PlainEncoder[User] = user => s"name=${user.name}, age=${user.age}"
  given Encoder[User]      = deriveEncoder

  "Circe integration" must:
    "use given instance with Encoder implementation for JsonEncoder" in:
      val user     = User("John", 18)
      val expected = """{"name":"John","age":18}"""

      Loggable.make[User]("user").json(user) shouldEqual expected
