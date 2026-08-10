package logging4s.json.play

import org.scalatest.wordspec.AnyWordSpec
import org.scalatest.matchers.should.Matchers

import play.api.libs.json.{Json, Writes}

import logging4s.core.{Loggable, PlainEncoder, PlainString}

import PlayJsonInstances.given

class PlayJsonIntegrationSpec extends AnyWordSpec, Matchers:

  final case class User(name: String, age: Int)

  given PlainEncoder[User] = user => PlainString(s"name=${user.name}, age=${user.age}")
  given Writes[User]       = Json.writes

  "Play-json integration" must:
    "use given instance with Writes implementation for JsonEncoder" in:
      val user     = User("John", 18)
      val expected = """{"name":"John","age":18}"""

      Loggable.fromEncoders[User]("user").json(user) shouldEqual expected
