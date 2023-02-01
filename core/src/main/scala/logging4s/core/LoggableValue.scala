package logging4s.core

final case class LoggableValue(key: String, plain: String, json: String)

object LoggableValue:

  given [T](using Loggable[T]): Conversion[T, LoggableValue] = v =>
    LoggableValue(
      key = Loggable[T].key,
      plain = Loggable[T].plain(v),
      json = Loggable[T].json(v),
    )

  object extensions:
    extension (values: Seq[LoggableValue]) def plain: String = values.map(v => s"${v.key} -> (${v.plain})").mkString(", ")
