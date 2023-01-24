package logging4s.core

trait Loggable[A]:
  def key(a: A): String
  def json(a: A): String
  def plain(a: A): String

object Loggable:
  inline def apply[A](using instance: Loggable[A]): Loggable[A] = instance

  def make[A: JsonEncoder: PlainEncoder](keyName: String): Loggable[A] =
    new Loggable[A]:
      override def key(a: A): String   = keyName
      override def json(a: A): String  = JsonEncoder[A].encode(a)
      override def plain(a: A): String = PlainEncoder[A].encode(a)

  given [T](using L: Loggable[T]): Loggable[Seq[T]] with
    override def key(a: Seq[T]): String   = a.headOption.fold("")(v => s"${L.key(v)}s")
    override def plain(a: Seq[T]): String = a.map(v => L.plain(v)).mkString("[", ",", "]")
    override def json(a: Seq[T]): String  = a.map(v => L.json(v)).mkString("[", ",", "]")

  given [T](using L: Loggable[T]): Loggable[List[T]] with
    override def key(a: List[T]): String   = a.headOption.fold("")(v => s"${L.key(v)}s")
    override def plain(a: List[T]): String = a.map(v => L.plain(v)).mkString("[", ",", "]")
    override def json(a: List[T]): String  = a.map(v => L.json(v)).mkString("[", ",", "]")

  object extensions:
    extension [A](a: A)(using L: Loggable[A])
      def renameLoggable(newKey: String): Loggable[A] =
        new Loggable[A]:
          override def key(a: A): String   = newKey
          override def plain(a: A): String = L.plain(a)
          override def json(a: A): String  = L.json(a)
