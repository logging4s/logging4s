package logging4s.core

final case class LoggableValue(key: String, plain: String, json: String)

object LoggableValue:

  given [T](using L: Loggable[T]): Conversion[T, LoggableValue] = v =>
    LoggableValue(
      key = L.key,
      plain = L.plain(v),
      json = L.json(v),
    )

  // because sometimes the compiler can't convert, for example, Loggable[List[T] to LoggableValue
  given [T, C[*]](using L: Loggable[C[T]]): Conversion[C[T], LoggableValue] = v =>
    LoggableValue(
      key = L.key,
      plain = L.plain(v),
      json = L.json(v),
    )

  object extensions:
    extension (values: Seq[LoggableValue]) def plain: String = values.map(v => s"${v.key} -> (${v.plain})").mkString(", ")

  // keeps the first occurrence of a duplicated key unsuffixed and suffixes the rest with _2, _3, ...
  def deduplicateKeys(values: Seq[LoggableValue]): Seq[LoggableValue] =
    val counts = values.groupBy(_.key).view.mapValues(_.size).toMap
    val seen   = scala.collection.mutable.Map.empty[String, Int].withDefaultValue(0)

    values.map { value =>
      if counts(value.key) == 1
      then value
      else
        seen(value.key) += 1
        if seen(value.key) == 1 then value else value.copy(key = s"${value.key}_${seen(value.key)}")
    }
