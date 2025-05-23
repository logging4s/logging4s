package logging4s.json.play

import org.scalatest.matchers.should.Matchers
import org.scalatest.wordspec.AnyWordSpec

import play.api.libs.json.{Json, Writes}

import logging4s.core.{Loggable, PlainEncoder}

import instances.given

class PlayJsonIntegrationSpec extends AnyWordSpec with Matchers:

  final case class User(name: String, age: Int)

  given PlainEncoder[User] = user => s"name=${user.name}, age=${user.age}"
  given Writes[User]       = Json.writes

  "Play-json integration" must:
    "use given instance with Writes implementation for JsonEncoder" in:
      val user     = User("John", 18)
      val expected = """{"name":"John","age":18}"""

      Loggable.make[User]("user").json(user) shouldEqual expected
