package logging4s.core

import LoggableValue.given

object syntax:

  extension [A](a: A)(using L: Loggable[A])
    def asLoggable: LoggableValue = LoggableValue(L.key, L.plain(a), L.json(a))

    def rename(updatedKey: String): Loggable[A] =
      new Loggable[A]:
        override def key: String         = updatedKey
        override def plain(a: A): String = L.plain(a)
        override def json(a: A): String  = L.json(a)

  extension [A](a: A)(using JE: JsonEncoder[A], PE: PlainEncoder[A])
    def withKey(key: String): LoggableValue =
      val L = Loggable.make(key)
      LoggableValue(L.key, L.plain(a), L.json(a))
