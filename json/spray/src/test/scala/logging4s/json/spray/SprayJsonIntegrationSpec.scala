package logging4s.json.spray

import org.scalatest.matchers.should.Matchers
import org.scalatest.wordspec.AnyWordSpec

import spray.json.*
import DefaultJsonProtocol.*

import logging4s.core.{Loggable, PlainEncoder}

import instances.given

class SprayJsonIntegrationSpec extends AnyWordSpec with Matchers:

  final case class User(name: String, age: Int)

  given PlainEncoder[User] = user => s"name=${user.name}, age=${user.age}"
  given JsonWriter[User]   = jsonFormat2(User.apply)

  "Spray-json integration" must:
    "use given instance with JsonFormat implementation for JsonEncoder" in:
      val user     = User("John", 18)
      val expected = user.toJson.compactPrint

      Loggable.make[User]("user").json(user) shouldEqual expected
