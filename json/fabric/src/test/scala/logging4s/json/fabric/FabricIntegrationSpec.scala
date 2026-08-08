package logging4s.json.fabric

import org.scalatest.wordspec.AnyWordSpec
import org.scalatest.matchers.should.Matchers

import fabric.rw.RW

import logging4s.core.{Loggable, PlainEncoder, PlainString}

import fabric.rw.given
import FabricInstances.given

class FabricIntegrationSpec extends AnyWordSpec, Matchers:

  final case class User(name: String, age: Int)

  given PlainEncoder[User] = user => PlainString(s"name=${user.name}, age=${user.age}")
  given RW[User]           = RW.derived

  "Fabric integration" must:
    "use given instance with RW implementation for JsonEncoder" in:
      val user     = User("John", 18)
      val expected = """{"name":"John","age":18}"""

      Loggable.make[User]("user").json(user) shouldEqual expected
