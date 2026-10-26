package logging4s.core

import org.scalatest.wordspec.AnyWordSpec
import org.scalatest.matchers.should.Matchers

import logging4s.core.syntax.all.*

class LogForgingSpec extends AnyWordSpec, Matchers:

  "Rendering the message text" must:
    "escape a newline inside a value, so it cannot forge a second log line" in:
      LogMessage.render("login", None, Seq("bob\nERROR admin logged in".asLogValue("user"))) shouldEqual
        """login: user -> (bob\nERROR admin logged in)"""

    "escape carriage returns and terminal escape sequences" in:
      LogMessage.render("x", None, Seq("a\rb\u001b[31mred".asLogValue("v"))) shouldEqual """x: v -> (a\rb\u001b[31mred)"""

    "escape the unicode line separators some viewers treat as newlines" in:
      LogMessage.render("x", None, Seq("a b\u0085c".asLogValue("v"))) shouldEqual "x: v -> (a\\u2028b\\u0085c)"

    "keep tabs and ordinary text untouched" in:
      LogMessage.render("x", None, Seq("a\tb".asLogValue("v"))) shouldEqual "x: v -> (a\tb)"

    "escape the cause's message as well, since it often carries user input" in:
      LogMessage.render("failed", Some(new NumberFormatException("For input string: \"1\nFAKE\"")), Nil) shouldEqual
        """failed: class=java.lang.NumberFormatException, message=For input string: "1\nFAKE""""

    "render a cause without a message as null" in:
      LogMessage.render("failed", Some(new RuntimeException()), Nil) shouldEqual "failed: class=java.lang.RuntimeException, message=null"

    "leave the static message itself alone, since it is the developer's own text" in:
      LogMessage.render("first\nsecond", None, Nil) shouldEqual "first\nsecond"

    "leave the JSON channel to JSON escaping" in:
      "bob\nERROR".asLogValue("user").json.value shouldEqual """"bob\nERROR""""
