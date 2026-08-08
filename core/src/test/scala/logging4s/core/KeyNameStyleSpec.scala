package logging4s.core

import org.scalatest.wordspec.AnyWordSpec
import org.scalatest.matchers.should.Matchers

import logging4s.core.config.KeyNameStyle.*

class KeyNameStyleSpec extends AnyWordSpec, Matchers:

  "KeyNameStyle" must:
    "leave the name untouched with AsIs" in:
      AsIs.format("UserName") shouldEqual "UserName"

    "convert to snake_case, splitting camelCase and acronyms" in:
      SnakeCase.format("User") shouldEqual "user"
      SnakeCase.format("UserName") shouldEqual "user_name"
      SnakeCase.format("userName") shouldEqual "user_name"
      SnakeCase.format("HTTPRequest") shouldEqual "http_request"

    "convert to kebab-case" in:
      KebabCase.format("UserName") shouldEqual "user-name"

    "convert to camelCase from any input" in:
      CamelCase.format("UserName") shouldEqual "userName"
      CamelCase.format("user_name") shouldEqual "userName"

    "convert to PascalCase from any input" in:
      PascalCase.format("user_name") shouldEqual "UserName"
      PascalCase.format("userName") shouldEqual "UserName"
