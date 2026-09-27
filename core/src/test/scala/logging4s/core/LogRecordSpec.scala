package logging4s.core

import org.scalatest.wordspec.AnyWordSpec
import org.scalatest.matchers.should.Matchers

import logging4s.core.config.{KeyNameStyle, LoggableEncodingConfig}

class LogRecordSpec extends AnyWordSpec, Matchers:

  private given LoggableEncodingConfig = LoggableEncodingConfig()

  private val position = Position("Spec.scala", 1)

  private def value(key: String, json: String): LoggableValue =
    LoggableValue(ValueKey(key), PlainString(json), JsonString(json))

  "LogRecord.prepare" must:
    "keep the source out of the message values but present in the structured ones" in:
      val record = LogRecord.prepare(Nil, Seq(value("a", "1")), position)

      record.values.map(_.key) shouldEqual Seq(ValueKey("a"))
      record.structured.map(_.key) shouldEqual Seq(ValueKey("a"), Position.Key)

    "move a user value out of the way of a reserved envelope name" in:
      val record = LogRecord.prepare(Nil, Seq(value("level", "\"forged\"")), position, LogRecord.ReservedKeys)

      record.values.map(_.key) shouldEqual Seq(ValueKey("level_2"))

    "keep a user-supplied source under its own name and suffix the captured one" in:
      val record = LogRecord.prepare(Nil, Seq(value("source", "\"mine\"")), position)

      record.structured.map(_.key) shouldEqual Seq(Position.Key, ValueKey("source_2"))
      record.structured.head.json shouldEqual "\"mine\""

    "leave a record without collisions untouched" in:
      val values = Seq(value("a", "1"), value("b", "2"))
      val record = LogRecord.prepare(Nil, values, position)

      record.values shouldBe theSameInstanceAs(values)

  "LogRecord.context" must:
    "treat two context keys that normalize to the same name as one" in:
      val first  = LogRecord.context(Nil, LoggingContext(value("userId", "1")))
      val second = LogRecord.context(first.values, LoggingContext(value("user_id", "2")))

      second.values.map(_.key) shouldEqual Seq(ValueKey("user_id"))
      second.values.head.json shouldEqual "2"

    "let a call-site value override a context value under the same normalized name" in:
      val context = LogRecord.context(Nil, LoggingContext(value("userId", "1")))
      val record  = LogRecord.prepare(context.values, Seq(value("user_id", "2")), position)

      record.values.map(_.key) shouldEqual Seq(ValueKey("user_id"))
      record.values.head.json shouldEqual "2"

  "LogRecord under a non-default key style" must:
    "still collapse context keys that normalize together" in:
      given LoggableEncodingConfig = LoggableEncodingConfig(keyNameStyle = KeyNameStyle.CamelCase)

      val first  = LogRecord.context(Nil, LoggingContext(value("user_id", "1")))
      val second = LogRecord.context(first.values, LoggingContext(value("userId", "2")))

      second.values.map(_.key) shouldEqual Seq(ValueKey("userId"))
      second.values.head.json shouldEqual "2"
