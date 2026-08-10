package logging4s.kyo

import org.scalatest.wordspec.AnyWordSpec
import org.scalatest.matchers.should.Matchers

import kyo.{Maybe, Render}

import logging4s.core.{JsonEncoder, JsonString, Loggable}

import KyoInstances.given

class KyoInstancesSpec extends AnyWordSpec, Matchers:

  case class User(name: String, age: Int)

  given Render[User]      = a => s"name=${a.name}, age=${a.age}"
  given JsonEncoder[User] = a => JsonString(s"""{"name": "${a.name}", "age": ${a.age}}""")
  given Loggable[User]    = Loggable.fromEncoders[User]("user")

  "Kyo data and render instances" should:
    "right work Loggable for custom types for Render" in:
      val L    = Loggable[User]
      val user = User("John", 33)

      L.json(user) shouldEqual """{"name": "John", "age": 33}"""
      L.plain(user) shouldEqual "name=John, age=33"

    "right work Loggable for Option[T]" in:
      val L    = Loggable[Option[User]]
      val user = Some(User("John", 33))

      L.json(user) shouldEqual """{"name": "John", "age": 33}"""
      L.plain(user) shouldEqual "name=John, age=33"

      L.json(None) shouldEqual ""
      L.plain(None) shouldEqual ""

    "right work Loggable for Maybe[T] derived from Option" in:
      val L    = Loggable[Maybe[User]]
      val user = Maybe.Present(User("John", 33))

      L.key shouldEqual "user"
      L.json(user) shouldEqual """{"name": "John", "age": 33}"""
      L.plain(user) shouldEqual "name=John, age=33"

      L.json(Maybe.Absent) shouldEqual ""
      L.plain(Maybe.Absent) shouldEqual ""
