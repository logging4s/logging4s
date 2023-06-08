package logging4s.json.json4s

import org.scalatest.matchers.should.Matchers
import org.scalatest.wordspec.AnyWordSpec

import org.json4s.{DefaultFormats, Formats}
import org.json4s.native.Serialization

import logging4s.core.{Loggable, PlainEncoder}

import instances.given

class Json4sIntegrationSpec extends AnyWordSpec with Matchers:

  final case class User(name: String, age: Int)

  given PlainEncoder[User] = user => s"name=${user.name}, age=${user.age}"
  given Formats            = DefaultFormats

  "Json4s integration" must:
    "use given Formats for implementation JsonEncoder" in:
      val user     = User("John", 18)
      val expected = Serialization.write(user)

      Loggable.make[User]("user").json(user) shouldEqual expected
