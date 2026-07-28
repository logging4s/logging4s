package logging4s.core

import java.time.{Instant, LocalDateTime, ZonedDateTime, ZoneOffset}
import java.util.UUID
import scala.concurrent.duration.*

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

    "right summon Char instances" in:
      Loggable[Char].plain('a') shouldEqual "a"
      Loggable[Char].json('a') shouldEqual "\"a\""

    "right summon BigDecimal instances" in:
      Loggable[BigDecimal].plain(BigDecimal("10.50")) shouldEqual "10.50"
      Loggable[BigDecimal].json(BigDecimal("10.50")) shouldEqual "10.50"

    "right summon BigInt instances" in:
      Loggable[BigInt].plain(BigInt(123)) shouldEqual "123"
      Loggable[BigInt].json(BigInt(123)) shouldEqual "123"

    "right summon UUID instances" in:
      val uuid = UUID.fromString("123e4567-e89b-12d3-a456-426614174000")

      Loggable[UUID].plain(uuid) shouldEqual "123e4567-e89b-12d3-a456-426614174000"
      Loggable[UUID].json(uuid) shouldEqual "\"123e4567-e89b-12d3-a456-426614174000\""

    "right summon Instant instances" in:
      Loggable[Instant].plain(Instant.EPOCH) shouldEqual "1970-01-01T00:00:00Z"
      Loggable[Instant].json(Instant.EPOCH) shouldEqual "\"1970-01-01T00:00:00Z\""

    "right summon LocalDateTime instances" in:
      val value = LocalDateTime.of(2023, 1, 30, 13, 42, 13)

      Loggable[LocalDateTime].plain(value) shouldEqual "2023-01-30T13:42:13"
      Loggable[LocalDateTime].json(value) shouldEqual "\"2023-01-30T13:42:13\""

    "right summon ZonedDateTime instances" in:
      val value = ZonedDateTime.of(2023, 1, 30, 13, 42, 13, 0, ZoneOffset.UTC)

      Loggable[ZonedDateTime].plain(value) shouldEqual "2023-01-30T13:42:13Z"
      Loggable[ZonedDateTime].json(value) shouldEqual "\"2023-01-30T13:42:13Z\""

    "right summon FiniteDuration instances" in:
      Loggable[FiniteDuration].key shouldEqual "time_ms"
      Loggable[FiniteDuration].plain(5.seconds) shouldEqual "5000"
      Loggable[FiniteDuration].json(5.seconds) shouldEqual "5000"

    "right summon Either instances" in:
      Loggable[Either[String, Int]].plain(Left("err")) shouldEqual "err"
      Loggable[Either[String, Int]].json(Left("err")) shouldEqual "\"err\""
      Loggable[Either[String, Int]].plain(Right(5)) shouldEqual "5"
      Loggable[Either[String, Int]].json(Right(5)) shouldEqual "5"
