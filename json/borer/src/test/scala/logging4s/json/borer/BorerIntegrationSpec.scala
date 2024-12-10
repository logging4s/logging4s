package logging4s.json.borer

import org.scalatest.matchers.should.Matchers
import org.scalatest.wordspec.AnyWordSpec

import io.bullet.borer.{Encoder, Json}

import logging4s.core.{Loggable, PlainEncoder}

import instances.given

class BorerIntegrationSpec extends AnyWordSpec with Matchers:

  final case class User(name: String, age: Int)

  given PlainEncoder[User] = user => s"name=${user.name}, age=${user.age}"
  given Encoder[User]      = Encoder.forProduct

  "Borer integration" must:
    "use given EncodeJson implementation for JsonEncoder" in:
      val user     = User("John", 18)
      val expected = Json.encode(user).toUtf8String

      Loggable.make[User]("user").json(user) shouldEqual expected
