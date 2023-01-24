package logging4s.json.jsoniter

import org.scalatest.matchers.should.Matchers
import org.scalatest.wordspec.AnyWordSpec

import com.github.plokhotnyuk.jsoniter_scala.core.{JsonValueCodec, writeToString}
import com.github.plokhotnyuk.jsoniter_scala.macros.JsonCodecMaker

import logging4s.core.{Loggable, PlainEncoder}

class JsoniterIntegrationSpec extends AnyWordSpec with Matchers:

  "Jsoniter integration" must {

    "use given instance with JsonCodec implementation for JsonEncoder" in {
      import instances.given

      final case class User(name: String, age: Int)

      given PlainEncoder[User]   = user => s"name=${user.name}, age=${user.age}"
      given JsonValueCodec[User] = JsonCodecMaker.make

      val user = User("John", 18)
      val expected = writeToString(user)

      Loggable.make[User]("user").json(user) shouldEqual expected
    }

  }
