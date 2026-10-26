package logging4s.core

import org.scalatest.wordspec.AnyWordSpec
import org.scalatest.matchers.should.Matchers

import logging4s.core.syntax.all.*
import logging4s.core.config.LoggableEncodingConfig

class OmitAbsentSpec extends AnyWordSpec, Matchers:
  import OmitAbsentSpec.*

  private val omitting = LoggableEncodingConfig(omitAbsentValues = true)

  "With omitAbsentValues off, the default" must:
    "render None as null" in:
      val record = LogRecord.prepare(Nil, Seq(Option.empty[Int].asLogValue("n")), Position.unknown)

      record.values.map(_.json.value) shouldEqual Seq("null")

    "keep an absent field of a derived product" in:
      Loggable.derived[Profile].json(Profile("bob", None)) shouldEqual """{"name":"bob","email":null}"""

  "With omitAbsentValues on" must:
    "drop a top-level None and keep a Some" in:
      given LoggableEncodingConfig = omitting
      val record                   = LogRecord.prepare(Nil, Seq(Option.empty[Int].asLogValue("n"), Some(1).asLogValue("m")), Position.unknown)

      record.values.map(_.key.value) shouldEqual Seq("m")
      record.structured.map(_.key.value) shouldEqual Seq("m", "source")

    "treat Some(None) as absent too" in:
      given LoggableEncodingConfig    = omitting
      val nested: Option[Option[Int]] = Some(None)

      LogRecord.prepare(Nil, Seq(nested.asLogValue("n")), Position.unknown).values shouldBe empty

    "let a call-site None remove a context value with the same key, exactly as it would override it with null" in:
      given LoggableEncodingConfig = omitting
      val record                   = LogRecord.prepare(Seq(Some(1).asLogValue("n")), Seq(Option.empty[Int].asLogValue("n")), Position.unknown)

      record.values shouldBe empty

    "decide absence without rendering the value" in:
      given LoggableEncodingConfig = omitting
      val counting                 = Counting()

      LogRecord.prepare(Nil, Seq(LoggableValue.deferred(ValueKey("n"), Option.empty[Int], counting)), Position.unknown)

      counting.rendered shouldEqual 0

    "omit an absent field of a derived product, in json and in plain" in:
      given LoggableEncodingConfig = omitting
      val loggable                 = Loggable.derived[Profile]

      loggable.json(Profile("bob", None)) shouldEqual """{"name":"bob"}"""
      loggable.plain(Profile("bob", None)) shouldEqual "name -> (bob)"
      loggable.json(Profile("bob", Some("b@x.io"))) shouldEqual """{"name":"bob","email":"b@x.io"}"""

  "Absence" must:
    "survive rename and contramap" in:
      Loggable[Option[Int]].rename("x").isAbsent(None) shouldEqual true
      Loggable[Option[Int]].contramap[Int](i => Option.when(i > 0)(i)).isAbsent(0) shouldEqual true
      Loggable[Option[Int]].contramap[Int](i => Option.when(i > 0)(i)).isAbsent(1) shouldEqual false

    "never apply to a redacted value, so redaction does not reveal whether a value was there" in:
      Loggable[Option[Int]].redacted().isAbsent(None) shouldEqual false

    "apply to a pre-rendered JSON null" in:
      LoggableValue(ValueKey("k"), PlainString(""), JsonString.Null).absent shouldEqual true
      LoggableValue(ValueKey("k"), PlainString("1"), JsonString("1")).absent shouldEqual false

object OmitAbsentSpec:
  final case class Profile(name: String, email: Option[String])

  final class Counting extends Loggable[Option[Int]]:
    var rendered = 0

    override val key: ValueKey                      = ValueKey("n")
    override def json(a: Option[Int]): JsonString   = { rendered += 1; Loggable[Option[Int]].json(a) }
    override def plain(a: Option[Int]): PlainString = { rendered += 1; Loggable[Option[Int]].plain(a) }
    override def isAbsent(a: Option[Int]): Boolean  = a.isEmpty
