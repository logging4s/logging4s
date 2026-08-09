package logging4s.core

import org.scalatest.wordspec.AnyWordSpec
import org.scalatest.matchers.should.Matchers

class JsonStringSpec extends AnyWordSpec, Matchers:

  "JsonString.quoted" must:
    "wrap a plain string untouched (fast path)" in:
      JsonString.quoted("hello").value shouldEqual "\"hello\""

    "escape double quotes" in:
      JsonString.quoted("she said \"hi\"").value shouldEqual "\"she said \\\"hi\\\"\""

    "escape backslashes, e.g. Windows paths" in:
      JsonString.quoted("C:\\Users\\out").value shouldEqual "\"C:\\\\Users\\\\out\""

    "escape newline, carriage return and tab" in:
      JsonString.quoted("a\nb\rc\td").value shouldEqual "\"a\\nb\\rc\\td\""

    "escape control characters as \\uXXXX" in:
      JsonString.quoted("\u0001\u001f").value shouldEqual "\"\\u0001\\u001f\""
