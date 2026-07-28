package logging4s.json.zio

import org.scalatest.matchers.should.Matchers
import org.scalatest.wordspec.AnyWordSpec

import zio.json.{JsonEncoder, DeriveJsonEncoder}

import logging4s.core.{Loggable, PlainEncoder}

import logging4s.json.zio.ZioJsonInstances.given

class ZioJsonIntegrationSpec extends AnyWordSpec with Matchers:

  final case class User(name: String, age: Int)

  given PlainEncoder[User] = user => s"name=${user.name}, age=${user.age}"
  given JsonEncoder[User]  = DeriveJsonEncoder.gen

  "Zio-json integration" must:
    "use given instance with zio.JsonEncoder implementation for JsonEncoder" in:
      val user     = User("John", 18)
      val expected = """{"name":"John","age":18}"""

      Loggable.make[User]("user").json(user) shouldEqual expected
