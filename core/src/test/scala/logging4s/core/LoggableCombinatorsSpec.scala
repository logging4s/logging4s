package logging4s.core

import org.scalatest.wordspec.AnyWordSpec
import org.scalatest.matchers.should.Matchers

import logging4s.core.syntax.all.*

object Probe:
  var json: Int  = 0
  var plain: Int = 0

  def reset(): Unit =
    json = 0
    plain = 0

  final case class Value(raw: String)

  given Loggable[Value] = Loggable.make[Value]("probe")(
    v =>
      json += 1
      JsonString.quoted(v.raw)
    ,
    v =>
      plain += 1
      PlainString(v.raw),
  )

  given JsonEncoder[Value] = v =>
    json += 1
    JsonString.quoted(v.raw)

  given PlainEncoder[Value] = v =>
    plain += 1
    PlainString(v.raw)

class LoggableCombinatorsSpec extends AnyWordSpec, Matchers:

  import Probe.given

  "mapPlain" must:
    "not render anything until the value is read" in:
      Probe.reset()

      val value = Probe.Value("x").mapPlain(_.toUpperCase)

      Probe.json shouldEqual 0
      Probe.plain shouldEqual 0

      value.plain shouldEqual "X"
      value.json shouldEqual "\"x\""

  "withKey over encoders" must:
    "not render anything until the value is read" in:
      Probe.reset()

      val value = Probe.Value("x").withKey("custom")

      Probe.json shouldEqual 0
      Probe.plain shouldEqual 0

      value.key shouldEqual ValueKey("custom")
      value.json shouldEqual "\"x\""

  "Loggable[Set]" must:
    "render equal sets identically regardless of insertion order" in:
      Loggable[Set[Int]].json(Set(1, 2, 3)) shouldEqual Loggable[Set[Int]].json(Set(3, 2, 1))
      Loggable[Set[Int]].json(Set(1, 2)) shouldEqual Loggable[Set[Int]].json(Set(2, 1))
      Loggable[Set[String]].plain(Set("a", "b")) shouldEqual Loggable[Set[String]].plain(Set("b", "a"))

  "Loggable.mapPlain combinator" must:
    "transform the plain rendering and keep json intact" in:
      val loggable = Loggable[String].mapPlain(_.toUpperCase)

      loggable.plain("abc") shouldEqual "ABC"
      loggable.json("abc") shouldEqual "\"abc\""
      loggable.key shouldEqual ValueKey("string")
