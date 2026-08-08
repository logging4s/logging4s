package logging4s.core

import java.util.UUID
import java.time.{Instant, LocalDateTime, ZonedDateTime}

import scala.concurrent.duration.FiniteDuration

trait Loggable[A]:
  self: Loggable[A] =>

  def key: ValueKey
  def json(a: A): JsonString
  def plain(a: A): PlainString

  def rename(updatedKey: String): Loggable[A] =
    new:
      override def key: ValueKey            = ValueKey(updatedKey)
      override def json(a: A): JsonString   = self.json(a)
      override def plain(a: A): PlainString = self.plain(a)

  final def contramap[B](f: B => A, keyName: String = key.value): Loggable[B] =
    new:
      override def key: ValueKey            = ValueKey(keyName)
      override def json(b: B): JsonString   = self.json(f(b))
      override def plain(b: B): PlainString = self.plain(f(b))

  final def redacted(mask: String = "***"): Loggable[A] =
    new:
      override def key: ValueKey            = self.key
      override def plain(a: A): PlainString = PlainString(mask)
      override def json(a: A): JsonString   = JsonString.quoted(mask)

object Loggable:
  inline def apply[A](using instance: Loggable[A]): Loggable[A] = instance

  def make[A: JsonEncoder: PlainEncoder](keyName: String): Loggable[A] =
    new:
      override def key: ValueKey            = ValueKey(keyName)
      override def json(a: A): JsonString   = JsonEncoder[A].encode(a)
      override def plain(a: A): PlainString = PlainEncoder[A].encode(a)

  def make[A](keyName: String)(
      encode: A => String,
      show: A => String
  ): Loggable[A] = new:
    override def key: ValueKey            = ValueKey(keyName)
    override def json(a: A): JsonString   = JsonString(encode(a))
    override def plain(a: A): PlainString = PlainString(show(a))

  private def fromAnyVal[A <: AnyVal]: Loggable[A] = new:
    override def key: ValueKey            = ValueKey("value")
    override def json(a: A): JsonString   = JsonString(a.toString)
    override def plain(a: A): PlainString = PlainString(a.toString)

  given LoggableString: Loggable[String] = new:
    override def key: ValueKey                 = ValueKey("value")
    override def plain(a: String): PlainString = PlainString(a)
    override def json(a: String): JsonString   = JsonString.quoted(a)

  given LoggableByte: Loggable[Byte]   = LoggableInt.contramap(_.toInt)
  given LoggableShort: Loggable[Short] = LoggableInt.contramap(_.toInt)

  given LoggableChar: Loggable[Char] = LoggableString.contramap(_.toString)

  given LoggableBoolean: Loggable[Boolean] = fromAnyVal
  given LoggableInt: Loggable[Int]         = fromAnyVal
  given LoggableLong: Loggable[Long]       = fromAnyVal
  given LoggableFloat: Loggable[Float]     = fromAnyVal
  given LoggableDouble: Loggable[Double]   = fromAnyVal

  given LoggableBigDecimal: Loggable[BigDecimal] = new:
    override def key: ValueKey                     = ValueKey("value")
    override def plain(a: BigDecimal): PlainString = PlainString(a.toString)
    override def json(a: BigDecimal): JsonString   = JsonString(a.toString)

  given LoggableBigInt: Loggable[BigInt] = new:
    override def key: ValueKey                 = ValueKey("value")
    override def plain(a: BigInt): PlainString = PlainString(a.toString)
    override def json(a: BigInt): JsonString   = JsonString(a.toString)

  given LoggableUUID: Loggable[UUID] = LoggableString.contramap(_.toString)

  given LoggableInstant: Loggable[Instant] = new:
    override def key: ValueKey                  = ValueKey("value")
    override def plain(a: Instant): PlainString = PlainString(a.toString)
    override def json(a: Instant): JsonString   = JsonString.quoted(a.toString)

  given LoggableLocalDateTime: Loggable[LocalDateTime] = new:
    override def key: ValueKey                        = ValueKey("value")
    override def plain(a: LocalDateTime): PlainString = PlainString(a.toString)
    override def json(a: LocalDateTime): JsonString   = JsonString.quoted(a.toString)

  given LoggableZonedDateTime: Loggable[ZonedDateTime] = new:
    override def key: ValueKey                        = ValueKey("value")
    override def plain(a: ZonedDateTime): PlainString = PlainString(a.toString)
    override def json(a: ZonedDateTime): JsonString   = JsonString.quoted(a.toString)

  given LoggableFiniteDuration: Loggable[FiniteDuration] = LoggableLong.contramap(_.toMillis, "time_ms")

  given LoggableUnit: Loggable[Unit] =
    new:
      override def key: ValueKey               = ValueKey("value")
      override def plain(u: Unit): PlainString = PlainString("")
      override def json(u: Unit): JsonString   = JsonString("")

  given LoggableOption[T](using L: Loggable[T]): Loggable[Option[T]] =
    new:
      override def key: ValueKey                    = L.key
      override def plain(t: Option[T]): PlainString = t.fold(PlainString(""))(L.plain)
      override def json(t: Option[T]): JsonString   = t.fold(JsonString(""))(L.json)

  given LoggableEither[A, B](using AL: Loggable[A], BL: Loggable[B]): Loggable[Either[A, B]] =
    new:
      override def key: ValueKey                       = ValueKey.combine(AL.key, BL.key)
      override def plain(e: Either[A, B]): PlainString = e.fold(AL.plain, BL.plain)
      override def json(e: Either[A, B]): JsonString   = e.fold(AL.json, BL.json)

  given LoggableTuple2[A, B](using AL: Loggable[A], BL: Loggable[B]): Loggable[(A, B)] =
    new:
      override def key: ValueKey                 = ValueKey.combine(AL.key, BL.key)
      override def plain(v: (A, B)): PlainString = PlainString.tuple(AL.plain(v._1), BL.plain(v._2))
      override def json(v: (A, B)): JsonString   = JsonString.array(AL.json(v._1), BL.json(v._2))

  given LoggableTuple3[A, B, C](using
      AL: Loggable[A],
      BL: Loggable[B],
      CL: Loggable[C]
  ): Loggable[(A, B, C)] =
    new:
      override def key: ValueKey                    = ValueKey.combine(AL.key, BL.key, CL.key)
      override def plain(v: (A, B, C)): PlainString = PlainString.tuple(AL.plain(v._1), BL.plain(v._2), CL.plain(v._3))
      override def json(v: (A, B, C)): JsonString   = JsonString.array(AL.json(v._1), BL.json(v._2), CL.json(v._3))

  given LoggableTuple4[A, B, C, D](using
      AL: Loggable[A],
      BL: Loggable[B],
      CL: Loggable[C],
      DL: Loggable[D]
  ): Loggable[(A, B, C, D)] =
    new:
      override def key: ValueKey =
        ValueKey.combine(AL.key, BL.key, CL.key, DL.key)

      override def plain(v: (A, B, C, D)): PlainString =
        PlainString.tuple(AL.plain(v._1), BL.plain(v._2), CL.plain(v._3), DL.plain(v._4))

      override def json(v: (A, B, C, D)): JsonString =
        JsonString.array(AL.json(v._1), BL.json(v._2), CL.json(v._3), DL.json(v._4))

  given LoggableTuple5[A, B, C, D, E](using
      AL: Loggable[A],
      BL: Loggable[B],
      CL: Loggable[C],
      DL: Loggable[D],
      EL: Loggable[E]
  ): Loggable[(A, B, C, D, E)] =
    new:
      override def key: ValueKey =
        ValueKey.combine(AL.key, BL.key, CL.key, DL.key, EL.key)

      override def plain(v: (A, B, C, D, E)): PlainString =
        PlainString.tuple(AL.plain(v._1), BL.plain(v._2), CL.plain(v._3), DL.plain(v._4), EL.plain(v._5))

      override def json(v: (A, B, C, D, E)): JsonString =
        JsonString.array(AL.json(v._1), BL.json(v._2), CL.json(v._3), DL.json(v._4), EL.json(v._5))

  given LoggableList[T](using L: Loggable[T]): Loggable[List[T]] =
    new:
      override def key: ValueKey                  = L.key
      override def plain(a: List[T]): PlainString = PlainString.array(a.map(L.plain)*)
      override def json(a: List[T]): JsonString   = JsonString.array(a.map(L.json)*)

  given LoggableVector[T](using L: Loggable[T]): Loggable[Vector[T]] =
    new:
      override def key: ValueKey                    = L.key
      override def plain(a: Vector[T]): PlainString = PlainString.array(a.map(L.plain)*)
      override def json(a: Vector[T]): JsonString   = JsonString.array(a.map(L.json)*)

  given LoggableSet[T](using L: Loggable[T]): Loggable[Set[T]] =
    new:
      override def key: ValueKey                 = L.key
      override def plain(a: Set[T]): PlainString = PlainString.array(a.map(L.plain).toSeq*)
      override def json(a: Set[T]): JsonString   = JsonString.array(a.map(L.json).toSeq*)

  given LoggableSeq[T](using L: Loggable[T]): Loggable[Seq[T]] =
    new:
      override def key: ValueKey                 = L.key
      override def plain(a: Seq[T]): PlainString = PlainString.array(a.map(L.plain)*)
      override def json(a: Seq[T]): JsonString   = JsonString.array(a.map(L.json)*)

  given LoggableMap[K, V](using KL: Loggable[K], VL: Loggable[V]): Loggable[Map[K, V]] =
    new:
      override def key: ValueKey                    = Loggable[(K, V)].key
      override def plain(a: Map[K, V]): PlainString = PlainString.array(a.toSeq.map(Loggable[(K, V)].plain)*)
      override def json(a: Map[K, V]): JsonString   = JsonString.array(a.toSeq.map(Loggable[(K, V)].json)*)

  given LoggableContainer[T, C[*]](using L: Loggable[T], ev: C[T] => Iterable[T]): Loggable[C[T]] =
    new:
      override def key: ValueKey               = L.key
      override def plain(a: C[T]): PlainString = PlainString.array(ev(a).toSeq.map(L.plain)*)
      override def json(a: C[T]): JsonString   = JsonString.array(ev(a).toSeq.map(L.json)*)
