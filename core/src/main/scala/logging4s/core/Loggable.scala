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

  given [T](using L: Loggable[T]): Loggable[Iterable[T]] with
    override def key: String                   = s"${L.key}s"
    override def plain(a: Iterable[T]): String = a.map(v => s"(${L.plain(v)})").mkString("[", ",", "]")
    override def json(a: Iterable[T]): String  = a.map(v => L.json(v)).mkString("[", ",", "]")

  given [K, V](using KL: Loggable[K], VL: Loggable[V]): Loggable[Map[K, V]] with
    override def key: String                 = Loggable[(K, V)].key
    override def plain(a: Map[K, V]): String = a.map(e => Loggable[(K, V)].plain(e)).mkString("[", ",", "]")
    override def json(a: Map[K, V]): String  = a.map(e => Loggable[(K, V)].json(e)).mkString("[", ",", "]")

  given [A, B](using AL: Loggable[A], BL: Loggable[B]): Loggable[(A, B)] with
    override def key: String              = s"${AL.key}_${BL.key}"
    override def plain(a: (A, B)): String = s"(${AL.plain(a._1)}, ${BL.plain(a._2)})"
    override def json(a: (A, B)): String  = s"""{"${AL.key}": ${AL.json(a._1)}, "${BL.key}": ${BL.json(a._2)}"""
