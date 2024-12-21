package logging4s.core

final case class LoggableValue(key: String, plain: String, json: String)

object LoggableValue:

  given [T: Loggable as L] => Conversion[T, LoggableValue] = v =>
    LoggableValue(
      key = L.key,
      plain = L.plain(v),
      json = L.json(v),
    )

  // because sometimes the compiler can't convert for example Loggable[List[T] to LoggableValue
  given [T, C[*]] => (L: Loggable[C[T]]) => Conversion[C[T], LoggableValue] = v =>
    LoggableValue(
      key = L.key,
      plain = L.plain(v),
      json = L.json(v),
    )

  object extensions:
    extension (values: Seq[LoggableValue]) def plain: String = values.map(v => s"${v.key} -> (${v.plain})").mkString(", ")
