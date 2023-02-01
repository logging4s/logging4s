package logging4s.core

object syntax:

  extension[A] (a: A)(using L: Loggable[A])
    def rename(updatedKey: String): Loggable[A] =
      new Loggable[A]:
        override def key: String = updatedKey

        override def plain(a: A): String = L.plain(a)

        override def json(a: A): String = L.json(a)

  extension[A] (a: A)(using JE: JsonEncoder[A], PE: PlainEncoder[A])
    def withKey(key: String): Loggable[A] =
      Loggable.make(key)
    
