package logging4s.core

import org.scalatest.wordspec.AnyWordSpec
import org.scalatest.matchers.should.Matchers

import logging4s.core.config.PlainValuesStyle.*

class PlainValuesStyleSpec extends AnyWordSpec, Matchers:

  private val values = Seq(
    LoggableValue(ValueKey("user"), PlainString("John"), JsonString("\"John\"")),
    LoggableValue(ValueKey("count"), PlainString("5"), JsonString("5")),
  )

  "PlainValuesStyle" must:
    "render Arrow" in:
      Arrow.render(values) shouldEqual "user -> (John), count -> (5)"

    "render ArrowBare" in:
      ArrowBare.render(values) shouldEqual "user -> John, count -> 5"

    "render Logfmt" in:
      Logfmt.render(values) shouldEqual "user=John count=5"

    "render KeyValueComma" in:
      KeyValueComma.render(values) shouldEqual "user=John, count=5"

    "render Colon" in:
      Colon.render(values) shouldEqual "user: John, count: 5"

    "render Bracketed" in:
      Bracketed.render(values) shouldEqual "[user=John] [count=5]"

    "render Parens" in:
      Parens.render(values) shouldEqual "user(John) count(5)"

    "render CurlyMap" in:
      CurlyMap.render(values) shouldEqual "{user=John, count=5}"
