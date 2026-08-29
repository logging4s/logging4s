package logging4s.core

import org.scalatest.wordspec.AnyWordSpec
import org.scalatest.matchers.should.Matchers

class ValueKeySpec extends AnyWordSpec, Matchers:

  "ValueKey.combine" must:
    "keep a single key as is" in:
      ValueKey.combine(ValueKey("user")) shouldEqual "user"

    "keep the shared key when every key is the same" in:
      ValueKey.combine(ValueKey("int"), ValueKey("int"), ValueKey("int")) shouldEqual "int"

    "join distinct keys with an underscore" in:
      ValueKey.combine(ValueKey("int"), ValueKey("time_ms")) shouldEqual "int_time_ms"

    "return an empty key instead of throwing when there is nothing to combine" in:
      ValueKey.combine() shouldEqual ""
