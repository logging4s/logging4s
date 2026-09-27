package logging4s.core

import org.scalatest.wordspec.AnyWordSpec
import org.scalatest.time.{Seconds, Span}
import org.scalatest.concurrent.TimeLimits
import org.scalatest.matchers.should.Matchers

final case class Node(value: Int, next: Option[Node]) derives Loggable

final case class Branch(name: String, children: List[Branch]) derives Loggable

final case class Odd(value: Int, even: Option[Even]) derives Loggable
final case class Even(value: Int, odd: Option[Odd]) derives Loggable

enum Expr derives Loggable:
  case Literal(value: Int)
  case Negate(inner: Expr)

class LoggableRecursionSpec extends AnyWordSpec, Matchers, TimeLimits:

  private val limit = Span(10, Seconds)

  "A recursive product" must:
    "render a leaf without hanging" in:
      failAfter(limit) {
        Loggable[Node].json(Node(1, None)) shouldEqual """{"value":1,"next":null}"""
      }

    "render a nested chain" in:
      failAfter(limit) {
        Loggable[Node].json(Node(1, Some(Node(2, None)))) shouldEqual """{"value":1,"next":{"value":2,"next":null}}"""
      }

    "render through a collection field" in:
      failAfter(limit) {
        Loggable[Branch].json(Branch("root", List(Branch("leaf", Nil)))) shouldEqual
          """{"name":"root","children":[{"name":"leaf","children":[]}]}"""
      }

  "Mutually recursive products" must:
    "render without hanging" in:
      failAfter(limit) {
        Loggable[Odd].json(Odd(1, Some(Even(2, None)))) shouldEqual """{"value":1,"even":{"value":2,"odd":null}}"""
      }

  "A recursive sum" must:
    "render without hanging" in:
      failAfter(limit) {
        Loggable[Expr].json(Expr.Negate(Expr.Literal(3))) shouldEqual """{"inner":{"value":3}}"""
      }
