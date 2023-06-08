package logging4s.json.circe

import org.scalatest.matchers.should.Matchers
import org.scalatest.wordspec.AnyWordSpec

import io.circe.Encoder
import io.circe.generic.semiauto.deriveEncoder

import logging4s.core.{Loggable, PlainEncoder}

import instances.given

class CirceIntegrationSpec extends AnyWordSpec with Matchers:

  final case class User(name: String, age: Int)

  "Circe integration" must:
    "use given instance with JsonCodec implementation for JsonEncoder" in:
      given PlainEncoder[User] = user => s"name=${user.name}, age=${user.age}"
      given Encoder[User]      = deriveEncoder

      val user     = User("John", 18)
      val expected = Encoder[User].apply(user).noSpaces

      Loggable.make[User]("user").json(user) shouldEqual expected
