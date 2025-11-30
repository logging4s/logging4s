package logging4s.kyo

import org.scalatest.matchers.should.Matchers
import org.scalatest.wordspec.AnyWordSpec

import kyo.Render

import logging4s.core.{JsonEncoder, Loggable}

import instances.given

class KyoInstancesSpec extends AnyWordSpec with Matchers:

  case class User(name: String, age: Int)

  given Render[User]      = a => s"name=${a.name}, age=${a.age}"
  given JsonEncoder[User] = a => s"""{"name": "${a.name}", "age": ${a.age}}"""
  given Loggable[User]    = Loggable.make[User]("user")

  "Kyo data and render instances" should:
    "right work Loggable for custom types for Render" in:
      val L    = Loggable[User]
      val user = User("John", 33)

      L.json(user) shouldEqual """{"name": "John", "age": 33}"""
      L.plain(user) shouldEqual "name=John, age=33"

    "right work Loggable for Maybe[T] for Render" in:
      val L    = Loggable[Option[User]]
      val user = Some(User("John", 33))

      L.json(user) shouldEqual """{"name": "John", "age": 33}"""
      L.plain(user) shouldEqual "name=John, age=33"

      L.json(None) shouldEqual ""
      L.plain(None) shouldEqual ""
