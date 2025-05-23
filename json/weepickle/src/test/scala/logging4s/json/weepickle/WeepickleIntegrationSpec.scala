package logging4s.json.weepickle

import org.scalatest.matchers.should.Matchers
import org.scalatest.wordspec.AnyWordSpec

import com.rallyhealth.weepickle.v1.WeePickle.{From, macroFrom}

import logging4s.core.{Loggable, PlainEncoder}

import instance.given

class WeepickleIntegrationSpec extends AnyWordSpec with Matchers:

  final case class User(name: String, age: Int)

  given PlainEncoder[User] = user => s"name=${user.name}, age=${user.age}"
  given From[User]         = macroFrom

  "Weepickle integration" must:
    "use given instance with From implementation for JsonEncoder" in:
      val user     = User("John", 18)
      val expected = """{"name":"John","age":18}"""

      Loggable.make[User]("user").json(user) shouldEqual expected
