package logging4s.core

import java.util.UUID
import java.time.{Instant, LocalDateTime, ZonedDateTime}

import scala.deriving.Mirror
import scala.concurrent.duration.FiniteDuration

import logging4s.core.config.LoggableEncodingConfig
import logging4s.core.deriving.{macros, LoggableBuilder}

trait Loggable[A]:
  self: Loggable[A] =>

  val key: ValueKey
  def json(a: A): JsonString
  def plain(a: A): PlainString

  final def rename(updatedKey: String): Loggable[A] =
    new:
      override val key: ValueKey            = ValueKey(updatedKey)
      override def json(a: A): JsonString   = self.json(a)
      override def plain(a: A): PlainString = self.plain(a)

  final def contramap[B](f: B => A, keyName: String = key.value): Loggable[B] =
    new:
      override val key: ValueKey            = ValueKey(keyName)
      override def json(b: B): JsonString   = self.json(f(b))
      override def plain(b: B): PlainString = self.plain(f(b))

  final def redacted(mask: String = "***"): Loggable[A] =
    new:
      override val key: ValueKey            = self.key
      override def plain(a: A): PlainString = PlainString(mask)
      override def json(a: A): JsonString   = JsonString.quoted(mask)

object Loggable:
  inline def apply[A](using instance: Loggable[A]): Loggable[A] = instance

  inline def derived[A](using Mirror.Of[A]): Loggable[A] = macros.derived[A]

  def deriving[A]: LoggableBuilder[A] = LoggableBuilder(Map.empty)

  inline def fromEncoders[A](using JsonEncoder[A]): Loggable[A] = macros.fromEncoders[A]

  def make[A: JsonEncoder: PlainEncoder](keyName: String): Loggable[A] =
    new:
      override val key: ValueKey            = ValueKey(keyName)
      override def json(a: A): JsonString   = JsonEncoder[A].encode(a)
      override def plain(a: A): PlainString = PlainEncoder[A].encode(a)

  def make[A](keyName: String)(
      encode: A => String,
      show: A => String
  ): Loggable[A] = new:
    override val key: ValueKey            = ValueKey(keyName)
    override def json(a: A): JsonString   = JsonString(encode(a))
    override def plain(a: A): PlainString = PlainString(show(a))

  private def fromAnyVal[A <: AnyVal](keyName: String): Loggable[A] = new:
    override val key: ValueKey            = ValueKey(keyName)
    override def json(a: A): JsonString   = JsonString(a.toString)
    override def plain(a: A): PlainString = PlainString(a.toString)

  given LoggableString: Loggable[String] = new:
    override val key: ValueKey                 = ValueKey("string")
    override def plain(a: String): PlainString = PlainString(a)
    override def json(a: String): JsonString   = JsonString.quoted(a)

  given LoggableByte: Loggable[Byte]   = LoggableInt.contramap(_.toInt, "byte")
  given LoggableShort: Loggable[Short] = LoggableInt.contramap(_.toInt, "short")

  given LoggableChar: Loggable[Char] = LoggableString.contramap(_.toString, "char")

  given LoggableBoolean: Loggable[Boolean] = fromAnyVal("boolean")
  given LoggableInt: Loggable[Int]         = fromAnyVal("int")
  given LoggableLong: Loggable[Long]       = fromAnyVal("long")
  given LoggableFloat: Loggable[Float]     = fromAnyVal("float")
  given LoggableDouble: Loggable[Double]   = fromAnyVal("double")

  given LoggableBigDecimal: Loggable[BigDecimal] = new:
    override val key: ValueKey                     = ValueKey("bigdecimal")
    override def plain(a: BigDecimal): PlainString = PlainString(a.toString)
    override def json(a: BigDecimal): JsonString   = JsonString(a.toString)

  given LoggableBigInt: Loggable[BigInt] = new:
    override val key: ValueKey                 = ValueKey("bigint")
    override def plain(a: BigInt): PlainString = PlainString(a.toString)
    override def json(a: BigInt): JsonString   = JsonString(a.toString)

  given LoggableUUID: Loggable[UUID] = LoggableString.contramap(_.toString, "uuid")

  given LoggableInstant: Loggable[Instant] = new:
    override val key: ValueKey                  = ValueKey("time")
    override def plain(a: Instant): PlainString = PlainString(a.toString)
    override def json(a: Instant): JsonString   = JsonString.quoted(a.toString)

  given LoggableLocalDateTime: Loggable[LocalDateTime] = new:
    override val key: ValueKey                        = ValueKey("time")
    override def plain(a: LocalDateTime): PlainString = PlainString(a.toString)
    override def json(a: LocalDateTime): JsonString   = JsonString.quoted(a.toString)

  given LoggableZonedDateTime: Loggable[ZonedDateTime] = new:
    override val key: ValueKey                        = ValueKey("time")
    override def plain(a: ZonedDateTime): PlainString = PlainString(a.toString)
    override def json(a: ZonedDateTime): JsonString   = JsonString.quoted(a.toString)

  given LoggableFiniteDuration: Loggable[FiniteDuration] = LoggableLong.contramap(_.toMillis, "time_ms")

  given LoggableUnit: Loggable[Unit] =
    new:
      override val key: ValueKey               = ValueKey("unit")
      override def plain(u: Unit): PlainString = PlainString("")
      override def json(u: Unit): JsonString   = JsonString("")

  given LoggableOption[T](using L: Loggable[T]): Loggable[Option[T]] =
    new:
      override val key: ValueKey                    = L.key
      override def plain(t: Option[T]): PlainString = t.fold(PlainString(""))(L.plain)
      override def json(t: Option[T]): JsonString   = t.fold(JsonString(""))(L.json)

  given LoggableEither[A, B](using AL: Loggable[A], BL: Loggable[B]): Loggable[Either[A, B]] =
    new:
      override val key: ValueKey                       = ValueKey.combine(AL.key, BL.key)
      override def plain(e: Either[A, B]): PlainString = e.fold(AL.plain, BL.plain)
      override def json(e: Either[A, B]): JsonString   = e.fold(AL.json, BL.json)

  given LoggableTuple2[A, B](using
      Loggable[A],
      Loggable[B],
      LoggableEncodingConfig,
  ): Loggable[(A, B)] =
    macros.deriveTuple

  given LoggableTuple3[A, B, C](using
      Loggable[A],
      Loggable[B],
      Loggable[C],
      LoggableEncodingConfig,
  ): Loggable[(A, B, C)] =
    macros.deriveTuple

  given LoggableTuple4[A, B, C, D](using
      Loggable[A],
      Loggable[B],
      Loggable[C],
      Loggable[D],
      LoggableEncodingConfig,
  ): Loggable[(A, B, C, D)] = macros.deriveTuple

  given LoggableTuple5[A, B, C, D, E](using
      Loggable[A],
      Loggable[B],
      Loggable[C],
      Loggable[D],
      Loggable[E],
      LoggableEncodingConfig,
  ): Loggable[(A, B, C, D, E)] = macros.deriveTuple

  given LoggableList[T](using L: Loggable[T]): Loggable[List[T]] =
    new:
      override val key: ValueKey                  = L.key.pluralized
      override def plain(a: List[T]): PlainString = PlainString.array(a.map(L.plain)*)
      override def json(a: List[T]): JsonString   = JsonString.array(a.map(L.json)*)

  given LoggableVector[T](using L: Loggable[T]): Loggable[Vector[T]] =
    new:
      override val key: ValueKey                    = L.key.pluralized
      override def plain(a: Vector[T]): PlainString = PlainString.array(a.map(L.plain)*)
      override def json(a: Vector[T]): JsonString   = JsonString.array(a.map(L.json)*)

  given LoggableSet[T](using L: Loggable[T]): Loggable[Set[T]] =
    new:
      override val key: ValueKey                 = L.key.pluralized
      override def plain(a: Set[T]): PlainString = PlainString.array(a.map(L.plain).toSeq*)
      override def json(a: Set[T]): JsonString   = JsonString.array(a.map(L.json).toSeq*)

  given LoggableSeq[T](using L: Loggable[T]): Loggable[Seq[T]] =
    new:
      override val key: ValueKey                 = L.key.pluralized
      override def plain(a: Seq[T]): PlainString = PlainString.array(a.map(L.plain)*)
      override def json(a: Seq[T]): JsonString   = JsonString.array(a.map(L.json)*)

  given LoggableMap[K, V](using KL: Loggable[K], VL: Loggable[V], cfg: LoggableEncodingConfig): Loggable[Map[K, V]] =
    new:
      override val key: ValueKey                    = Loggable[(K, V)].key.pluralized
      override def plain(a: Map[K, V]): PlainString = PlainString.array(a.toSeq.map(Loggable[(K, V)].plain)*)
      override def json(a: Map[K, V]): JsonString   = JsonString.array(a.toSeq.map(Loggable[(K, V)].json)*)

  given LoggableContainer[T, C[*]](using L: Loggable[T], ev: C[T] => Iterable[T]): Loggable[C[T]] =
    new:
      override val key: ValueKey               = L.key.pluralized
      override def plain(a: C[T]): PlainString = PlainString.array(ev(a).toSeq.map(L.plain)*)
      override def json(a: C[T]): JsonString   = JsonString.array(ev(a).toSeq.map(L.json)*)
