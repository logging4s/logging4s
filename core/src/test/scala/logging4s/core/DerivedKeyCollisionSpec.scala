package logging4s.core

import org.scalatest.wordspec.AnyWordSpec
import org.scalatest.matchers.should.Matchers

import logging4s.core.config.LoggableEncodingConfig

final case class Clashing(userId: Int, user_id: Int) derives Loggable

final case class Inner(id: Int, label: String) derives Loggable
final case class OuterClash(id: Int, inner: Inner)
final case class OuterFree(name: String, inner: Inner)

class DerivedKeyCollisionSpec extends AnyWordSpec, Matchers:

  private def keysOf(json: String): List[String] =
    """"([^"]+)":""".r.findAllMatchIn(json).map(_.group(1)).toList

  "A derived product" must:
    "keep field names unique when two fields normalize to the same key" in:
      val rendered = Loggable[Clashing].json(Clashing(1, 2)).value
      val keys     = keysOf(rendered)

      keys.distinct.size shouldEqual keys.size
      rendered should include("1")
      rendered should include("2")

  "A tuple rendered as an object" must:
    "keep element names unique when two elements share a key" in:
      given LoggableEncodingConfig = LoggableEncodingConfig(jsonTupleAsArray = false)

      val rendered = Loggable[(Int, Int)].json((1, 2)).value
      val keys     = keysOf(rendered)

      keys.distinct.size shouldEqual keys.size
      rendered shouldEqual """{"int":1,"int_2":2}"""

  "unembed" must:
    "fall back to nesting rather than splice a field name the parent already uses" in:
      val loggable = Loggable.deriving[OuterClash].unembed(_.inner).derived

      loggable.json(OuterClash(1, Inner(2, "x"))) shouldEqual """{"id":1,"inner":{"id":2,"label":"x"}}"""

    "still flatten when the inner fields do not collide" in:
      val loggable = Loggable.deriving[OuterFree].unembed(_.inner).derived

      loggable.json(OuterFree("n", Inner(2, "x"))) shouldEqual """{"name":"n","id":2,"label":"x"}"""
