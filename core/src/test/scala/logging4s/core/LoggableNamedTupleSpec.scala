package logging4s.core

import org.scalatest.wordspec.AnyWordSpec
import org.scalatest.matchers.should.Matchers

import logging4s.core.config.{KeyNameStyle, LoggableEncodingConfig}

class LoggableNamedTupleSpec extends AnyWordSpec, Matchers:

  "A named tuple" must:
    "render as an object keyed by its field names" in:
      val user = (id = 1, name = "John")

      Loggable[(id: Int, name: String)].json(user) shouldEqual """{"id":1,"name":"John"}"""
      Loggable[(id: Int, name: String)].plain(user) shouldEqual "id -> (1), name -> (John)"

    "key itself by the joined field names" in:
      Loggable[(id: Int, name: String)].key shouldEqual "id_name"

    "run its field names through keyNameStyle, like a derived product" in:
      given LoggableEncodingConfig = LoggableEncodingConfig(keyNameStyle = KeyNameStyle.SnakeCase)

      Loggable[(userId: Int)].json((userId = 1)) shouldEqual """{"user_id":1}"""

    "nest inside a derived product" in:
      Loggable[(outer: String, inner: (a: Int, b: Int))].json((outer = "x", inner = (a = 1, b = 2))) shouldEqual
        """{"outer":"x","inner":{"a":1,"b":2}}"""

    "work through the syntax extension with an explicit key" in:
      import logging4s.core.syntax.all.*

      val value = (id = 7, active = true).asLogValue("session")

      value.key shouldEqual ValueKey("session")
      value.json shouldEqual """{"id":7,"active":true}"""

    "support arities past the Tuple5 limit of plain tuples" in:
      val wide = (a = 1, b = 2, c = 3, d = 4, e = 5, f = 6, g = 7)

      Loggable[(a: Int, b: Int, c: Int, d: Int, e: Int, f: Int, g: Int)].json(wide) shouldEqual
        """{"a":1,"b":2,"c":3,"d":4,"e":5,"f":6,"g":7}"""
