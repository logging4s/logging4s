package logging4s.core

import org.scalatest.wordspec.AnyWordSpec
import org.scalatest.matchers.should.Matchers

import logging4s.core.syntax.*

class LoggableSpec extends AnyWordSpec with Matchers:

  "Loggable" must:
    "auto summon Seq and List instances" in:
      Loggable[Seq[String]].key shouldEqual "values"
      Loggable[Seq[String]].plain(Seq("a", "b", "c")) shouldEqual "[a,b,c]"
      Loggable[Seq[String]].json(Seq("a", "b", "c")) shouldEqual """["a","b","c"]"""

    "right rename key with extension" in:
      "data".rename("test").key shouldEqual "test"

    "right rename key with the trait method" in:
      val renamed = Loggable[Int].rename("count")

      renamed.key shouldEqual "count"
      renamed.plain(5) shouldEqual "5"
      renamed.json(5) shouldEqual "5"

    "right contramap to another type" in:
      final case class Age(value: Int)

      val loggable = Loggable[Int].contramap[Age](_.value, "age")

      loggable.key shouldEqual "age"
      loggable.plain(Age(30)) shouldEqual "30"
      loggable.json(Age(30)) shouldEqual "30"

    "right build via the encode/show overload of make" in:
      final case class Money(cents: Int)

      val loggable = Loggable.make[Money]("money")(m => (m.cents / 100.0).toString, m => s"$$${m.cents / 100.0}")

      loggable.key shouldEqual "money"
      loggable.json(Money(1050)) shouldEqual "10.5"
      loggable.plain(Money(1050)) shouldEqual "$10.5"

    "right convert collection of tuples" in:
      val data = List(
        1 -> "v1",
        2 -> "v2",
        3 -> "v3",
      )

      val lv = data.asLogValue("data")

      lv.key shouldEqual "data"
      lv.plain shouldEqual "[(1, v1),(2, v2),(3, v3)]"
      lv.json shouldEqual """[[1,"v1"],[2,"v2"],[3,"v3"]]"""

    "right build a Tuple3 instance" in:
      Loggable[(Int, String, Boolean)].plain((1, "a", true)) shouldEqual "(1, a, true)"
      Loggable[(Int, String, Boolean)].json((1, "a", true)) shouldEqual """[1,"a",true]"""

    "right summon Option instances" in:
      Loggable[Option[Int]].plain(Some(5)) shouldEqual "5"
      Loggable[Option[Int]].json(Some(5)) shouldEqual "5"
      Loggable[Option[Int]].plain(None) shouldEqual ""
      Loggable[Option[Int]].json(None) shouldEqual ""

    "right summon Set instances regardless of element order" in:
      val loggable = Loggable[Set[Int]]

      loggable.key shouldEqual "values"
      loggable.plain(Set(1)) shouldEqual "[1]"
      loggable.json(Set(1)) shouldEqual "[1]"

    "right summon Map instances" in:
      Loggable[Map[String, Int]].plain(Map("a" -> 1)) shouldEqual "[(a, 1)]"
      Loggable[Map[String, Int]].json(Map("a" -> 1)) shouldEqual """[["a",1]]"""
