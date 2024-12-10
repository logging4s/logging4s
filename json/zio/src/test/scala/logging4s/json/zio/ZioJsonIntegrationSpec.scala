package logging4s.json.zio

import org.scalatest.matchers.should.Matchers
import org.scalatest.wordspec.AnyWordSpec

import zio.json.{JsonEncoder, DeriveJsonEncoder}

import logging4s.core.{Loggable, PlainEncoder}

import logging4s.json.zio.instances.given

class ZioJsonIntegrationSpec extends AnyWordSpec with Matchers:

  final case class User(name: String, age: Int)

  "Zio-json integration" must:
    "use given instance with zio.JsonEncoder implementation for JsonEncoder" in:
      given PlainEncoder[User] = user => s"name=${user.name}, age=${user.age}"
      given JsonEncoder[User]  = DeriveJsonEncoder.gen

      val user     = User("John", 18)
      val expected = JsonEncoder[User].encodeJson(user).toString

      Loggable.make[User]("user").json(user) shouldEqual expected
