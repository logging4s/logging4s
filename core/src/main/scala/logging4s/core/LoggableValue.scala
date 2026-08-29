package logging4s.core

import logging4s.core.config.{KeyNameStyle, LoggableEncodingConfig}

sealed trait LoggableValue:
  def key: ValueKey
  def plain: PlainString
  def json: JsonString

  def withKey(updatedKey: ValueKey): LoggableValue

object LoggableValue:

  def apply(key: ValueKey, plain: PlainString, json: JsonString): LoggableValue = Rendered(key, plain, json)

  def deferred[A](key: ValueKey, value: A, loggable: Loggable[A]): LoggableValue = Deferred(key, value, loggable)

  private final case class Rendered(key: ValueKey, plain: PlainString, json: JsonString) extends LoggableValue:
    override def withKey(updatedKey: ValueKey): LoggableValue = copy(key = updatedKey)

  private final class Deferred[A](val key: ValueKey, value: A, loggable: Loggable[A]) extends LoggableValue:
    override lazy val plain: PlainString = loggable.plain(value)
    override lazy val json: JsonString   = loggable.json(value)

    override def withKey(updatedKey: ValueKey): LoggableValue = Deferred(updatedKey, value, loggable)

  def normalizeKeys(values: Seq[LoggableValue])(using cfg: LoggableEncodingConfig): Seq[LoggableValue] =
    if cfg.keyNameStyle == KeyNameStyle.AsIs
    then values
    else values.map(value => value.withKey(ValueKey(cfg.keyNameStyle.format(value.key.value))))

  given [T](using L: Loggable[T]): Conversion[T, LoggableValue] = v => deferred(L.key, v, L)

  given [T, C[*]](using L: Loggable[C[T]]): Conversion[C[T], LoggableValue] = v => deferred(L.key, v, L)

  def deduplicateKeys(values: Seq[LoggableValue]): Seq[LoggableValue] =
    val counts = values.groupBy(_.key).view.mapValues(_.size).toMap
    val seen   = scala.collection.mutable.Map.empty[ValueKey, Int].withDefaultValue(0)

    values.map { value =>
      if counts(value.key) == 1
      then value
      else
        seen(value.key) += 1
        if seen(value.key) == 1 then value else value.withKey(value.key.suffixed(seen(value.key)))
    }
  end deduplicateKeys
