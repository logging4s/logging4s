package logging4s.core

import org.scalatest.wordspec.AnyWordSpec
import org.scalatest.matchers.should.Matchers

import logging4s.core.config.{KeyNameStyle, LoggableEncodingConfig}

final case class Point(x: Int, y: Int) derives Loggable
final case class Account(userId: Int, cardNumber: String) derives Loggable
final case class Address(city: String, zip: String) derives Loggable
final case class Person(name: String, address: Address) derives Loggable

enum Color derives Loggable:
  case Red, Green, Blue

sealed trait Event derives Loggable
object Event:
  final case class Click(x: Int, y: Int) extends Event
  case object Reset                      extends Event

final case class Shirt(size: Int, color: Color) derives Loggable
final case class Wrapper(owner: Account) derives Loggable
final case class Line(start: Point, points: List[Point]) derives Loggable

class LoggableDerivationSpec extends AnyWordSpec, Matchers:

  "Loggable.derived for a product" must:
    "key by the decapitalized type name" in:
      Loggable[Point].key shouldEqual "point"

    "render JSON as an object keyed by field names" in:
      Loggable[Point].json(Point(1, 2)) shouldEqual """{"x":1,"y":2}"""

    "render plain via the configured plainValuesStyle" in:
      Loggable[Point].plain(Point(1, 2)) shouldEqual "x -> (1), y -> (2)"

    "apply keyNameStyle to field names (default SnakeCase)" in:
      Loggable[Account].json(Account(1, "4242")) shouldEqual """{"user_id":1,"card_number":"4242"}"""

    "nest derived products" in:
      Loggable[Person].json(Person("Jane", Address("NYC", "10001"))) shouldEqual
        """{"name":"Jane","address":{"city":"NYC","zip":"10001"}}"""

    "bake a user keyNameStyle at the explicit derive site" in:
      given LoggableEncodingConfig = LoggableEncodingConfig(keyNameStyle = KeyNameStyle.CamelCase)

      Loggable.derived[Account].json(Account(1, "4242")) shouldEqual """{"userId":1,"cardNumber":"4242"}"""

  "Loggable.derived for a sum" must:
    "render a parameterless enum case as its label" in:
      Loggable[Color].key shouldEqual "color"
      Loggable[Color].json(Color.Green) shouldEqual "\"Green\""
      Loggable[Color].plain(Color.Red) shouldEqual "Red"

    "delegate to the concrete variant, product or singleton" in:
      Loggable[Event].json(Event.Click(1, 2)) shouldEqual """{"x":1,"y":2}"""
      Loggable[Event].json(Event.Reset) shouldEqual "\"Reset\""
      Loggable[Event].plain(Event.Click(1, 2)) shouldEqual "x -> (1), y -> (2)"

  "Loggable.derived recursively, reusing existing instances" must:
    "auto-derive a sum's case-class child that has no given of its own" in:
      Loggable[Event].json(Event.Click(3, 4)) shouldEqual """{"x":3,"y":4}"""

    "embed a derived product inside a collection, pluralizing the element key" in:
      Loggable[List[Point]].key shouldEqual "points"
      Loggable[List[Point]].json(List(Point(1, 2), Point(3, 4))) shouldEqual """[{"x":1,"y":2},{"x":3,"y":4}]"""

    "embed a derived product inside Option" in:
      Loggable[Option[Point]].json(Some(Point(1, 2))) shouldEqual """{"x":1,"y":2}"""
      Loggable[Option[Point]].json(None) shouldEqual ""

    "use a derived enum as a product field" in:
      Loggable[Shirt].json(Shirt(42, Color.Red)) shouldEqual """{"size":42,"color":"Red"}"""

    "normalize nested field names with keyNameStyle too" in:
      Loggable[Wrapper].json(Wrapper(Account(1, "4242"))) shouldEqual """{"owner":{"user_id":1,"card_number":"4242"}}"""

    "combine nested products with collections of products" in:
      Loggable[Line].json(Line(Point(0, 0), List(Point(1, 1)))) shouldEqual
        """{"start":{"x":0,"y":0},"points":[{"x":1,"y":1}]}"""

    "render nested products in plain form via plainValuesStyle" in:
      Loggable[Person].plain(Person("Jane", Address("NYC", "10001"))) shouldEqual
        "name -> (Jane), address -> (city -> (NYC), zip -> (10001))"
