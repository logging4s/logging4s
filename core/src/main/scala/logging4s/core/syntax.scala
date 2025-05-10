package logging4s.core

object syntax:

  extension [A](a: A)(using L: Loggable[A])
    def asLogValue: LoggableValue              = LoggableValue(L.key, L.plain(a), L.json(a))
    def asLogValue(key: String): LoggableValue = LoggableValue(key, L.plain(a), L.json(a))

    def rename(updatedKey: String): Loggable[A] = new:
        override def key: String         = updatedKey
        override def plain(a: A): String = L.plain(a)
        override def json(a: A): String  = L.json(a)

  extension [A](a: A)(using JsonEncoder[A], PlainEncoder[A])
    def withKey(key: String): LoggableValue =
      val L = Loggable.make(key)
      LoggableValue(L.key, L.plain(a), L.json(a))
