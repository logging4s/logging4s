package logging4s.json.circe

import io.circe.parser.parse

import org.scalatest.matchers.should.Matchers
import org.scalatest.wordspec.AnyWordSpec
import org.scalatestplus.scalacheck.ScalaCheckPropertyChecks

import org.scalacheck.{Arbitrary, Gen}

import logging4s.core.{JsonString, Loggable}

final case class Profile(name: String, nickname: Option[String], tags: List[String], score: Int) derives Loggable

object LoggableJsonValiditySpec:

  private val controlChar: Gen[Char] = Gen.chooseNum(0, 0x1f).map(_.toChar)

  val nastyString: Gen[String] =
    Gen
      .listOf(Gen.oneOf(Gen.asciiChar, Gen.const('"'), Gen.const('\\'), controlChar, Arbitrary.arbitrary[Char]))
      .map(_.mkString)

  given Arbitrary[String] = Arbitrary(nastyString)

  given Arbitrary[Profile] = Arbitrary(
    for
      name     <- nastyString
      nickname <- Gen.option(nastyString)
      tags     <- Gen.listOf(nastyString)
      score    <- Arbitrary.arbitrary[Int]
    yield Profile(name, nickname, tags, score)
  )

  given Arbitrary[Throwable] = Arbitrary(Gen.option(nastyString).map(message => new RuntimeException(message.orNull)))

class LoggableJsonValiditySpec extends AnyWordSpec, Matchers, ScalaCheckPropertyChecks:

  import LoggableJsonValiditySpec.given

  private def beValidJson(json: JsonString): Unit =
    parse(json.value) match
      case Right(_)    => ()
      case Left(error) => fail(s"not valid json: ${json.value} (${error.message})")

  "Every Loggable" must:
    "encode a string to valid json whatever it contains" in:
      forAll((value: String) => beValidJson(Loggable[String].json(value)))

    "encode an optional value to valid json" in:
      forAll((value: Option[Int]) => beValidJson(Loggable[Option[Int]].json(value)))

    "encode a collection of optional values to valid json" in:
      forAll((value: List[Option[String]]) => beValidJson(Loggable[List[Option[String]]].json(value)))

    "encode a derived product to valid json" in:
      forAll((value: Profile) => beValidJson(Loggable[Profile].json(value)))

    "encode a collection of derived products to valid json" in:
      forAll((value: List[Profile]) => beValidJson(Loggable[List[Profile]].json(value)))

    "encode a map to valid json" in:
      forAll((value: Map[String, Int]) => beValidJson(Loggable[Map[String, Int]].json(value)))

    "encode a tuple to valid json" in:
      forAll((value: (Int, String)) => beValidJson(Loggable[(Int, String)].json(value)))

    "encode a throwable to valid json" in:
      forAll((value: Throwable) => beValidJson(Loggable[Throwable].json(value)))

    "encode scalar values to valid json" in:
      forAll((value: Int) => beValidJson(Loggable[Int].json(value)))
      forAll((value: Long) => beValidJson(Loggable[Long].json(value)))
      forAll((value: Boolean) => beValidJson(Loggable[Boolean].json(value)))
      forAll((value: Double) => beValidJson(Loggable[Double].json(value)))
