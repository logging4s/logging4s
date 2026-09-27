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
    def formatted(value: LoggableValue): String = cfg.keyNameStyle.format(value.key.value)

    if cfg.keyNameStyle == KeyNameStyle.AsIs || !values.exists(value => formatted(value) != value.key.value)
    then values
    else
      values.map { value =>
        val name = formatted(value)
        if name == value.key.value then value else value.withKey(ValueKey(name))
      }

  given [T] => (L: Loggable[T]) => Conversion[T, LoggableValue] = v => deferred(L.key, v, L)

  given [T, C[*]] => (L: Loggable[C[T]]) => Conversion[C[T], LoggableValue] = v => deferred(L.key, v, L)

  def deduplicateKeys(values: Seq[LoggableValue], reserved: Set[ValueKey] = Set.empty): Seq[LoggableValue] =
    val counts = values.groupBy(_.key).view.mapValues(_.size).toMap

    if counts.size == values.size && !counts.keysIterator.exists(reserved.contains)
    then values
    else
      val taken = scala.collection.mutable.HashSet.from(values.iterator.map(_.key))
      val seen  = scala.collection.mutable.Map.empty[ValueKey, Int].withDefaultValue(0)

      taken ++= reserved

      values.map { value =>
        seen(value.key) += 1

        val firstOccurrence = seen(value.key) == 1

        if firstOccurrence && !reserved.contains(value.key)
        then value
        else
          var index     = if firstOccurrence then 2 else seen(value.key)
          var candidate = value.key.suffixed(index)

          while taken.contains(candidate) do
            index += 1
            candidate = value.key.suffixed(index)

          taken += candidate
          value.withKey(candidate)
      }
  end deduplicateKeys

  def withSource(values: Seq[LoggableValue], position: Position)(using cfg: LoggableEncodingConfig): Seq[LoggableValue] =
    if !cfg.includeSourcePosition
    then values
    else if !values.exists(_.key == Position.Key)
    then values :+ Position.asLogValue(position)
    else
      val taken = values.iterator.map(_.key).toSet
      var index = 2

      while taken.contains(Position.Key.suffixed(index)) do index += 1

      values :+ Position.asLogValue(position).withKey(Position.Key.suffixed(index))

  def mergeByKey(earlier: Seq[LoggableValue], later: Seq[LoggableValue]): Seq[LoggableValue] =
    if earlier.isEmpty then later
    else if later.isEmpty then earlier
    else
      val overridden = later.iterator.map(_.key).toSet
      earlier.filterNot(value => overridden.contains(value.key)) ++ later

  def keepLastByKey(values: Seq[LoggableValue]): Seq[LoggableValue] =
    if values.sizeIs <= 1 then values
    else
      val merged = scala.collection.mutable.LinkedHashMap.empty[ValueKey, LoggableValue]
      values.foreach(value => merged.update(value.key, value))
      if merged.size == values.size then values else merged.values.toSeq
