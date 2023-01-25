package logging4s.json.argonaut

import org.scalatest.matchers.should.Matchers
import org.scalatest.wordspec.AnyWordSpec

import argonaut.{EncodeJson, Argonaut}

import logging4s.core.{Loggable, PlainEncoder}

class ArgonautIntegrationSpec extends AnyWordSpec with Matchers:

  "Argonaut integration" must {

    "use given EncodeJson implementation for JsonEncoder" in {
      import instances.given
      import Argonaut.given

      final case class User(name: String, age: Int)

      given PlainEncoder[User] = user => s"name=${user.name}, age=${user.age}"
      given EncodeJson[User]   = Argonaut.jencode2L((u: User) => (u.name, u.age))("name", "age")

      val user     = User("John", 18)
      val expected = user.jencode.nospaces

      Loggable.make[User]("user").json(user) shouldEqual expected
    }

  }
