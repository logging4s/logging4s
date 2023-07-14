package logging4s.core

trait Loggable[A]:
  self: Loggable[A] =>

  def key: String
  def json(a: A): String
  def plain(a: A): String

  final def contramap[B](f: B => A, keyName: String): Loggable[B] =
    new Loggable[B]:
      override def key: String         = keyName
      override def json(b: B): String  = self.json(f(b))
      override def plain(b: B): String = self.plain(f(b))

object Loggable:
  inline def apply[A](using instance: Loggable[A]): Loggable[A] = instance

  def make[A: JsonEncoder: PlainEncoder](keyName: String): Loggable[A] =
    new Loggable[A]:
      override def key: String         = keyName
      override def json(a: A): String  = JsonEncoder[A].encode(a)
      override def plain(a: A): String = PlainEncoder[A].encode(a)

  private def fromAnyVal[A <: AnyVal]: Loggable[A] =
    new Loggable[A]:
      override def key: String         = "value"
      override def json(a: A): String  = a.toString
      override def plain(a: A): String = a.toString

  given Loggable[String] with
    override def key: String              = "value"
    override def plain(a: String): String = a
    override def json(a: String): String  = s"\"$a\""

  given Loggable[Byte] with
    override def key: String            = "value"
    override def plain(a: Byte): String = a.toInt.toString
    override def json(a: Byte): String  = a.toInt.toString

  given Loggable[Short] with
    override def key: String             = "value"
    override def plain(a: Short): String = a.toInt.toString
    override def json(a: Short): String  = a.toInt.toString

  given Loggable[Boolean] = fromAnyVal
  given Loggable[Int]     = fromAnyVal
  given Loggable[Long]    = fromAnyVal
  given Loggable[Float]   = fromAnyVal
  given Loggable[Double]  = fromAnyVal

  given [T](using L: Loggable[T]): Loggable[Option[T]] with
    override def key: String                 = L.key
    override def plain(t: Option[T]): String = t.fold("")(L.plain)
    override def json(t: Option[T]): String  = t.fold("")(L.json)

  given [A, B](using AL: Loggable[A], BL: Loggable[B]): Loggable[(A, B)] with
    override def key: String              = s"${AL.key}_${BL.key}"
    override def plain(a: (A, B)): String = s"(${AL.plain(a._1)}, ${BL.plain(a._2)})"
    override def json(a: (A, B)): String  = s"""{"${AL.key}": ${AL.json(a._1)}, "${BL.key}": ${BL.json(a._2)}}"""

  given [T](using L: Loggable[T]): Loggable[List[T]] with
    override def key: String               = s"${L.key}s"
    override def plain(a: List[T]): String = a.map(v => s"${L.plain(v)}").mkString("[", ",", "]")
    override def json(a: List[T]): String  = a.map(v => L.json(v)).mkString("[", ",", "]")

  given [T](using L: Loggable[T]): Loggable[Set[T]] with
    override def key: String              = s"${L.key}s"
    override def plain(a: Set[T]): String = a.map(v => s"${L.plain(v)}").mkString("[", ",", "]")
    override def json(a: Set[T]): String  = a.map(v => L.json(v)).mkString("[", ",", "]")

  given [T](using L: Loggable[T]): Loggable[Seq[T]] with
    override def key: String              = s"${L.key}s"
    override def plain(a: Seq[T]): String = a.map(v => s"${L.plain(v)}").mkString("[", ",", "]")
    override def json(a: Seq[T]): String  = a.map(v => L.json(v)).mkString("[", ",", "]")

  given [K, V](using KL: Loggable[K], VL: Loggable[V]): Loggable[Map[K, V]] with
    override def key: String                 = Loggable[(K, V)].key
    override def plain(a: Map[K, V]): String = a.map(e => Loggable[(K, V)].plain(e)).mkString("[", ",", "]")
    override def json(a: Map[K, V]): String  = a.map(e => Loggable[(K, V)].json(e)).mkString("[", ",", "]")

  given [T, C[*]](using L: Loggable[T], ev: C[T] => Iterable[T]): Loggable[C[T]] with
    override def key: String            = s"${L.key}s"
    override def plain(a: C[T]): String = ev(a).map(v => s"${L.plain(v)}").mkString("[", ",", "]")
    override def json(a: C[T]): String  = ev(a).map(v => L.json(v)).mkString("[", ",", "]")
