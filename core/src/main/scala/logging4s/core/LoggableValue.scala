package logging4s.core

import logging4s.core.config.{KeyNameStyle, LoggableEncodingConfig}

final case class LoggableValue(key: ValueKey, plain: PlainString, json: JsonString)

object LoggableValue:

  def normalizeKeys(values: Seq[LoggableValue])(using cfg: LoggableEncodingConfig): Seq[LoggableValue] =
    if cfg.keyNameStyle == KeyNameStyle.AsIs
    then values
    else values.map(value => value.copy(key = ValueKey(cfg.keyNameStyle.format(value.key.value))))

  given [T](using L: Loggable[T]): Conversion[T, LoggableValue] = v =>
    LoggableValue(
      key = L.key,
      plain = L.plain(v),
      json = L.json(v),
    )

  given [T, C[*]](using L: Loggable[C[T]]): Conversion[C[T], LoggableValue] = v =>
    LoggableValue(
      key = L.key,
      plain = L.plain(v),
      json = L.json(v),
    )

  def deduplicateKeys(values: Seq[LoggableValue]): Seq[LoggableValue] =
    val counts = values.groupBy(_.key).view.mapValues(_.size).toMap
    val seen   = scala.collection.mutable.Map.empty[ValueKey, Int].withDefaultValue(0)

    values.map { value =>
      if counts(value.key) == 1
      then value
      else
        seen(value.key) += 1
        if seen(value.key) == 1 then value else value.copy(key = value.key.suffixed(seen(value.key)))
    }
  end deduplicateKeys
