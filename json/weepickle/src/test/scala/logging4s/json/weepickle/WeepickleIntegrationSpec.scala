package logging4s.json.weepickle

import org.scalatest.matchers.should.Matchers
import org.scalatest.wordspec.AnyWordSpec

import com.rallyhealth.weejson.v1.jackson.ToJson
import com.rallyhealth.weepickle.v1.WeePickle.{From, FromScala, macroFrom}

import logging4s.core.{Loggable, PlainEncoder}

import instance.given

class WeepickleIntegrationSpec extends AnyWordSpec with Matchers:

  final case class User(name: String, age: Int)

  "Weepickle integration" must:
    "use given instance with From implementation for JsonEncoder" in:

      given PlainEncoder[User] = user => s"name=${user.name}, age=${user.age}"
      given From[User]         = macroFrom

      val user     = User("John", 18)
      val expected = FromScala(user).transform(ToJson.string)

      Loggable.make[User]("user").json(user) shouldEqual expected
