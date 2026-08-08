package logging4s.core

object syntax:

  extension [A](a: A)(using L: Loggable[A])
    def asLogValue: LoggableValue                    = LoggableValue(L.key, L.plain(a), L.json(a))
    def asLogValue(key: String): LoggableValue       = LoggableValue(ValueKey(key), L.plain(a), L.json(a))
    def mapPlain(f: String => String): LoggableValue = LoggableValue(L.key, PlainString(f(L.plain(a).value)), L.json(a))

  extension [A](a: A)(using JE: JsonEncoder[A], PE: PlainEncoder[A])
    def withKey(key: String): LoggableValue = LoggableValue(ValueKey(key), PE.encode(a), JE.encode(a))

  extension (values: Seq[LoggableValue]) def plain: String = values.map(v => s"${v.key} -> (${v.plain})").mkString(", ")
