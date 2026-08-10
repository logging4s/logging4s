package logging4s.json.weepickle

import org.scalatest.wordspec.AnyWordSpec
import org.scalatest.matchers.should.Matchers

import com.rallyhealth.weepickle.v1.WeePickle.{From, macroFrom}

import logging4s.core.{Loggable, PlainEncoder, PlainString}

import WeepickleInstances.given

class WeepickleIntegrationSpec extends AnyWordSpec, Matchers:

  final case class User(name: String, age: Int)

  given PlainEncoder[User] = user => PlainString(s"name=${user.name}, age=${user.age}")
  given From[User]         = macroFrom

  "Weepickle integration" must:
    "use given instance with From implementation for JsonEncoder" in:
      val user     = User("John", 18)
      val expected = """{"name":"John","age":18}"""

      Loggable.fromEncoders[User]("user").json(user) shouldEqual expected
