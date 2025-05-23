package logging4s.json.jsoniter

import org.scalatest.matchers.should.Matchers
import org.scalatest.wordspec.AnyWordSpec

import com.github.plokhotnyuk.jsoniter_scala.core.JsonValueCodec
import com.github.plokhotnyuk.jsoniter_scala.macros.JsonCodecMaker

import logging4s.core.{Loggable, PlainEncoder}

import instances.given

class JsoniterIntegrationSpec extends AnyWordSpec with Matchers:

  final case class User(name: String, age: Int)

  given PlainEncoder[User]   = user => s"name=${user.name}, age=${user.age}"
  given JsonValueCodec[User] = JsonCodecMaker.make

  "Jsoniter integration" must:
    "use given instance with JsonCodec implementation for JsonEncoder" in:
      val user     = User("John", 18)
      val expected = """{"name":"John","age":18}"""

      Loggable.make[User]("user").json(user) shouldEqual expected
