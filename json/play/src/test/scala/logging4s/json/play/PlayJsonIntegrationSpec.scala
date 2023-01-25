package logging4s.json.play

import org.scalatest.matchers.should.Matchers
import org.scalatest.wordspec.AnyWordSpec

import play.api.libs.json.{Json, Writes}

import logging4s.core.{Loggable, PlainEncoder}

class PlayJsonIntegrationSpec extends AnyWordSpec with Matchers:

  "Play-json integration" must {

    "use given instance with JsonCodec implementation for JsonEncoder" in {
      import instances.given

      final case class User(name: String, age: Int)

      given PlainEncoder[User] = user => s"name=${user.name}, age=${user.age}"
      given Writes[User]       = Json.writes

      val user     = User("John", 18)
      val expected = Json.toJson(user).toString

      Loggable.make[User]("user").json(user) shouldEqual expected
    }

  }
