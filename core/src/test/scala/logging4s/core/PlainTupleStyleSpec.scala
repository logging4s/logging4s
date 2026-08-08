package logging4s.core

import org.scalatest.wordspec.AnyWordSpec
import org.scalatest.matchers.should.Matchers

import logging4s.core.config.PlainTupleStyle.*

class PlainTupleStyleSpec extends AnyWordSpec, Matchers:

  private val elements = Seq(PlainString("1"), PlainString("John"))

  "PlainTupleStyle" must:
    "render AsScala" in:
      AsScala.render(elements).value shouldEqual "(1, John)"

    "render AsArray" in:
      AsArray.render(elements).value shouldEqual "[1, John]"

    "render Bare" in:
      Bare.render(elements).value shouldEqual "1, John"

    "render Braces" in:
      Braces.render(elements).value shouldEqual "{1, John}"
