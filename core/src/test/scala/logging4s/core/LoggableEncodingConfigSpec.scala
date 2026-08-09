package logging4s.core

import scala.concurrent.duration.FiniteDuration

import org.scalatest.wordspec.AnyWordSpec
import org.scalatest.matchers.should.Matchers

import logging4s.core.config.{KeyNameStyle, LoggableEncodingConfig, PlainTupleStyle, PlainValuesStyle}
import logging4s.core.syntax.all.plain

import scala.concurrent.duration.given

class LoggableEncodingConfigSpec extends AnyWordSpec, Matchers:

  "The default LoggableEncodingConfig given" must:
    "be resolvable without any user-provided given" in:
      summon[LoggableEncodingConfig] shouldEqual LoggableEncodingConfig.Default

    "render tuples AsScala with a JSON array" in:
      Loggable[(Int, String)].plain((1, "a")) shouldEqual "(1, a)"
      Loggable[(Int, String)].json((1, "a")) shouldEqual """[1,"a"]"""

  "A user-provided given (per-value styles, bound at the tuple summon site)" must:
    "override the tuple plain style" in:
      given LoggableEncodingConfig = LoggableEncodingConfig(plainTupleStyle = PlainTupleStyle.AsArray)

      Loggable[(Int, String)].plain((1, "a")) shouldEqual "[1, a]"

    "switch tuple JSON from an array to an object keyed by the element keys" in:
      given LoggableEncodingConfig = LoggableEncodingConfig(jsonTupleAsArray = false)

      Loggable[(Int, FiniteDuration)].json((1, 5.seconds)) shouldEqual """{"int":1,"time_ms":5000}"""

    "propagate into Map, which renders as an array of tuples" in:
      given LoggableEncodingConfig = LoggableEncodingConfig(plainTupleStyle = PlainTupleStyle.Braces)

      Loggable[Map[String, Int]].plain(Map("a" -> 1)) shouldEqual "[{a, 1}]"

  "KeyNameStyle via LoggableValue.normalizeKeys (aggregation side)" must:
    "reformat every key with the configured style" in:
      given LoggableEncodingConfig = LoggableEncodingConfig(keyNameStyle = KeyNameStyle.CamelCase)

      val values = Seq(
        LoggableValue(ValueKey("user_name"), PlainString("John"), JsonString("\"John\"")),
        LoggableValue(ValueKey("retry_count"), PlainString("3"), JsonString("3")),
      )

      LoggableValue.normalizeKeys(values).map(_.key) shouldEqual Seq(ValueKey("userName"), ValueKey("retryCount"))

    "return the very same Seq instance for AsIs, allocating nothing" in:
      given LoggableEncodingConfig = LoggableEncodingConfig(keyNameStyle = KeyNameStyle.AsIs)

      val values = Seq(LoggableValue(ValueKey("user_name"), PlainString("John"), JsonString("\"John\"")))

      LoggableValue.normalizeKeys(values) should be theSameInstanceAs values

  "PlainValuesStyle via the Seq[LoggableValue].plain extension" must:
    "join values with the configured style" in:
      given LoggableEncodingConfig = LoggableEncodingConfig(plainValuesStyle = PlainValuesStyle.Logfmt)

      val values = Seq(
        LoggableValue(ValueKey("user"), PlainString("John"), JsonString("\"John\"")),
        LoggableValue(ValueKey("count"), PlainString("5"), JsonString("5")),
      )

      values.plain shouldEqual "user=John count=5"

  "The full aggregation pipeline, as the backends run it" must:
    "apply keyNameStyle and plainValuesStyle together" in:
      given LoggableEncodingConfig =
        LoggableEncodingConfig(keyNameStyle = KeyNameStyle.KebabCase, plainValuesStyle = PlainValuesStyle.Logfmt)

      val values = Seq(
        LoggableValue(ValueKey("userName"), PlainString("John"), JsonString("\"John\"")),
        LoggableValue(ValueKey("retryCount"), PlainString("3"), JsonString("3")),
      )

      LoggableValue.normalizeKeys(values).plain shouldEqual "user-name=John retry-count=3"
