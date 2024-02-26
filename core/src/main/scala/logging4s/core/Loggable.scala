package logging4s.core

trait Loggable[A]:
  self: Loggable[A] =>

  def key: String
  def json(a: A): String
  def plain(a: A): String

  def rename(updatedKey: String): Loggable[A] = new Loggable[A]:
    override def key: String         = updatedKey
    override def json(a: A): String  = self.json(a)
    override def plain(a: A): String = self.plain(a)

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

  def make[A](keyName: String)(
      encode: A => String,
      show: A => String
  ): Loggable[A] = new Loggable[A]:
    override def key: String         = keyName
    override def json(a: A): String  = encode(a)
    override def plain(a: A): String = show(a)

  private def fromAnyVal[A <: AnyVal]: Loggable[A] =
    new Loggable[A]:
      override def key: String         = "value"
      override def json(a: A): String  = a.toString
      override def plain(a: A): String = a.toString

  given LoggableString: Loggable[String] with
    override def key: String              = "value"
    override def plain(a: String): String = a
    override def json(a: String): String  = s"\"$a\""

  given LoggableByte: Loggable[Byte] with
    override def key: String            = "value"
    override def plain(a: Byte): String = a.toInt.toString
    override def json(a: Byte): String  = a.toInt.toString

  given LoggableShort: Loggable[Short] with
    override def key: String             = "value"
    override def plain(a: Short): String = a.toInt.toString
    override def json(a: Short): String  = a.toInt.toString

  given LoggableBoolean: Loggable[Boolean] = fromAnyVal
  given LoggableInt: Loggable[Int]         = fromAnyVal
  given LoggableLong: Loggable[Long]       = fromAnyVal
  given LoggableFloat: Loggable[Float]     = fromAnyVal
  given LoggableDouble: Loggable[Double]   = fromAnyVal

  given LoggableOption[T](using L: Loggable[T]): Loggable[Option[T]] with
    override def key: String                 = L.key
    override def plain(t: Option[T]): String = t.fold("")(L.plain)
    override def json(t: Option[T]): String  = t.fold("")(L.json)

  given LoggableTuple[A <: Tuple]: Loggable[Tuple] = ???

  given LoggableTuple2[A, B](using AL: Loggable[A], BL: Loggable[B]): Loggable[(A, B)] with
    override def key: String              = if AL.key == BL.key then AL.key else s"${AL.key}_${BL.key}"
    override def plain(v: (A, B)): String = s"(${AL.plain(v._1)}, ${BL.plain(v._2)})"
    override def json(v: (A, B)): String  = s"[${AL.json(v._1)},${BL.json(v._2)}]"

  given LoggableTuple3[A, B, C](using
      AL: Loggable[A],
      BL: Loggable[B],
      CL: Loggable[C]
  ): Loggable[(A, B, C)] with
    override def key: String                 =
      if AL.key == BL.key && BL.key == CL.key
      then AL.key
      else s"${AL.key}_${BL.key}_${CL.key}"
    override def plain(v: (A, B, C)): String = s"(${AL.plain(v._1)}, ${BL.plain(v._2)}, ${CL.plain(v._3)})"
    override def json(v: (A, B, C)): String  = s"[${AL.json(v._1)},${BL.json(v._2)},${CL.json(v._3)}]"

  given LoggableTuple4[A, B, C, D](using
      AL: Loggable[A],
      BL: Loggable[B],
      CL: Loggable[C],
      DL: Loggable[D]
  ): Loggable[(A, B, C, D)] with
    override def key: String                    =
      if AL.key == BL.key && BL.key == CL.key && CL.key == DL.key
      then AL.key
      else s"${AL.key}_${BL.key}_${CL.key}_${DL.key}"
    override def plain(v: (A, B, C, D)): String = s"(${AL.plain(v._1)}, ${BL.plain(v._2)}, ${CL.plain(v._3)}, ${DL.plain(v._4)})"
    override def json(v: (A, B, C, D)): String  = s"[${AL.json(v._1)},${BL.json(v._2)},${CL.json(v._3)},${DL.json(v._4)}]"

  given LoggableTuple5[A, B, C, D, E](using
      AL: Loggable[A],
      BL: Loggable[B],
      CL: Loggable[C],
      DL: Loggable[D],
      EL: Loggable[E]
  ): Loggable[(A, B, C, D, E)] with
    override def key: String                       =
      if AL.key == BL.key && BL.key == CL.key && CL.key == DL.key && DL.key == EL.key
      then AL.key
      else s"${AL.key}_${BL.key}_${CL.key}_${DL.key}_${EL.key}"
    override def plain(v: (A, B, C, D, E)): String =
      s"(${AL.plain(v._1)}, ${BL.plain(v._2)}, ${CL.plain(v._3)}, ${DL.plain(v._4)}, ${EL.plain(v._5)})"
    override def json(v: (A, B, C, D, E)): String  = s"[${AL.json(v._1)},${BL.json(v._2)},${CL.json(v._3)},${DL.json(v._4)},${EL.json(v._5)}]"

  given LoggableList[T](using L: Loggable[T]): Loggable[List[T]] with
    override def key: String               = s"${L.key}s"
    override def plain(a: List[T]): String = a.map(v => s"${L.plain(v)}").mkString("[", ",", "]")
    override def json(a: List[T]): String  = a.map(v => L.json(v)).mkString("[", ",", "]")

  given LoggableSet[T](using L: Loggable[T]): Loggable[Set[T]] with
    override def key: String              = s"${L.key}s"
    override def plain(a: Set[T]): String = a.map(v => s"${L.plain(v)}").mkString("[", ",", "]")
    override def json(a: Set[T]): String  = a.map(v => L.json(v)).mkString("[", ",", "]")

  given LoggableSeq[T](using L: Loggable[T]): Loggable[Seq[T]] with
    override def key: String              = s"${L.key}s"
    override def plain(a: Seq[T]): String = a.map(v => s"${L.plain(v)}").mkString("[", ",", "]")
    override def json(a: Seq[T]): String  = a.map(v => L.json(v)).mkString("[", ",", "]")

  given LoggableMap[K, V](using KL: Loggable[K], VL: Loggable[V]): Loggable[Map[K, V]] with
    override def key: String                 = Loggable[(K, V)].key
    override def plain(a: Map[K, V]): String = a.map(e => Loggable[(K, V)].plain(e)).mkString("[", ",", "]")
    override def json(a: Map[K, V]): String  = a.map(e => Loggable[(K, V)].json(e)).mkString("[", ",", "]")

  given LoggableContainer[T, C[*]](using L: Loggable[T], ev: C[T] => Iterable[T]): Loggable[C[T]] with
    override def key: String            = s"${L.key}"
    override def plain(a: C[T]): String = ev(a).map(v => s"${L.plain(v)}").mkString("[", ",", "]")
    override def json(a: C[T]): String  = ev(a).map(v => L.json(v)).mkString("[", ",", "]")
