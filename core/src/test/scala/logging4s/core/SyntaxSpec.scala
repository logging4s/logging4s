package logging4s.core

import org.scalatest.wordspec.AnyWordSpec
import org.scalatest.matchers.should.Matchers

import logging4s.core.syntax.all.*

class SyntaxSpec extends AnyWordSpec, Matchers:

  "syntax" must:
    "build a LoggableValue via asLogValue using the summoned key" in:
      val lv = 5.asLogValue

      lv.key shouldEqual "int"
      lv.plain shouldEqual "5"
      lv.json shouldEqual "5"

    "build a LoggableValue via asLogValue with a custom key" in:
      val lv = "hello".asLogValue("greeting")

      lv.key shouldEqual "greeting"
      lv.plain shouldEqual "hello"
      lv.json shouldEqual "\"hello\""

    "build a LoggableValue via mapPlain transforming only the plain rendering" in:
      val lv = "hello world".mapPlain(_.take(5))

      lv.key shouldEqual "string"
      lv.plain shouldEqual "hello"
      lv.json shouldEqual "\"hello world\""

    "build a LoggableValue via withKey from JsonEncoder/PlainEncoder" in:
      given JsonEncoder[Int]  = i => JsonString(i.toString)
      given PlainEncoder[Int] = i => PlainString(s"n=$i")

      val lv = 42.withKey("answer")

      lv.key shouldEqual "answer"
      lv.plain shouldEqual "n=42"
      lv.json shouldEqual "42"
