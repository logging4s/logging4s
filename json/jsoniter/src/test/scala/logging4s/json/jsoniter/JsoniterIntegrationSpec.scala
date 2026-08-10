package logging4s.json.jsoniter

import org.scalatest.wordspec.AnyWordSpec
import org.scalatest.matchers.should.Matchers

import com.github.plokhotnyuk.jsoniter_scala.core.JsonValueCodec
import com.github.plokhotnyuk.jsoniter_scala.macros.JsonCodecMaker

import logging4s.core.{Loggable, PlainEncoder, PlainString}

import JsoniterInstances.given

class JsoniterIntegrationSpec extends AnyWordSpec, Matchers:

  final case class User(name: String, age: Int)

  given PlainEncoder[User]   = user => PlainString(s"name=${user.name}, age=${user.age}")
  given JsonValueCodec[User] = JsonCodecMaker.make

  "Jsoniter integration" must:
    "use given instance with JsonCodec implementation for JsonEncoder" in:
      val user     = User("John", 18)
      val expected = """{"name":"John","age":18}"""

      Loggable.fromEncoders[User]("user").json(user) shouldEqual expected
