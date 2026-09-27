package logging4s.core.deriving.internal

import scala.deriving.Mirror

import logging4s.core.*
import logging4s.core.deriving.FieldPolicy
import logging4s.core.config.LoggableEncodingConfig

private[internal] def decapitalize(name: String): String =
  if name.isEmpty
  then name
  else s"${name.head.toLower}${name.tail}"

private final case class FieldSpec(
    loggable: Loggable[Any],
    key: String,
    quotedKey: String,
    policy: FieldPolicy,
)

private[internal] def uniqueNames(names: List[String]): List[String] =
  if names.distinct.sizeIs == names.size
  then names
  else
    val taken = scala.collection.mutable.HashSet.from(names)
    val seen  = scala.collection.mutable.Map.empty[String, Int].withDefaultValue(0)

    names.map { name =>
      seen(name) += 1

      if seen(name) == 1
      then name
      else
        var index = seen(name)

        while taken.contains(s"${name}_$index") do index += 1

        val candidate = s"${name}_$index"
        taken += candidate
        candidate
    }

private def nestedNames(loggable: Loggable[Any]): Option[List[String]] =
  loggable match
    case product: ProductLoggable[?] => Some(product.fieldNames)
    case _                           => None

final class TupleLoggable[T <: Tuple](codecs: => List[Loggable[Any]], cfg: LoggableEncodingConfig) extends Loggable[T]:

  private lazy val loggables: List[Loggable[Any]] = codecs

  private lazy val fieldKeys: List[String] = uniqueNames(loggables.map(_.key.value))

  override lazy val key: ValueKey = ValueKey.combine(loggables.map(_.key)*)

  override def plain(t: T): PlainString =
    val elements = t.productIterator
    cfg.plainTupleStyle.render(loggables.map(_.plain(elements.next())))

  override def json(t: T): JsonString =
    val elements = t.productIterator
    if cfg.jsonTupleAsArray
    then JsonString.array(loggables.map(_.json(elements.next()))*)
    else JsonString.obj(fieldKeys.lazyZip(loggables).map((name, l) => name -> l.json(elements.next()))*)

final class ProductLoggable[A](
    typeName: String,
    labels: List[String],
    codecs: => List[Loggable[Any]],
    policies: Map[String, FieldPolicy],
    cfg: LoggableEncodingConfig,
) extends Loggable[A]:

  override val key: ValueKey = ValueKey(decapitalize(typeName))

  private[internal] lazy val fieldNames: List[String] =
    specs.filterNot(_.policy.hidden).flatMap { spec =>
      if spec.policy.unembedded
      then nestedNames(spec.loggable).getOrElse(List(spec.key))
      else List(spec.key)
    }

  private lazy val specs: List[FieldSpec] =
    val drafts = labels.lazyZip(codecs).map { (label, loggable) =>
      val policy = policies.getOrElse(label, FieldPolicy.none)
      (loggable, cfg.keyNameStyle.format(policy.name.getOrElse(label)), policy)
    }

    val ownKeys = drafts.collect { case (_, key, policy) if !policy.hidden && !policy.unembedded => key }

    val settled = drafts
      .foldLeft((List.empty[(Loggable[Any], String, FieldPolicy)], ownKeys.toSet)) { case ((done, claimed), draft) =>
        val (loggable, key, policy) = draft

        if policy.hidden || !policy.unembedded
        then (draft :: done, claimed)
        else
          nestedNames(loggable) match
            case Some(inner) if inner.exists(claimed.contains) => ((loggable, key, policy.copy(unembedded = false)) :: done, claimed + key)
            case Some(inner)                                   => (draft :: done, claimed ++ inner)
            case None                                          => (draft :: done, claimed)
      }
      ._1
      .reverse

    val materialized = settled.zipWithIndex.collect {
      case ((_, key, policy), index) if !policy.hidden && !policy.unembedded => index -> key
    }

    val renamed = materialized.map(_._1).zip(uniqueNames(materialized.map(_._2))).toMap

    settled.zipWithIndex.map { case ((loggable, key, policy), index) =>
      val fieldKey = renamed.getOrElse(index, key)

      FieldSpec(
        loggable = loggable,
        key = fieldKey,
        quotedKey = JsonString.quoted(fieldKey).value,
        policy = policy,
      )
    }
  end specs

  override def json(a: A): JsonString =
    if specs.isEmpty
    then JsonString.quoted(typeName)
    else
      val fields = a.asInstanceOf[Product].productIterator
      val sb     = new StringBuilder("{")
      var first  = true

      def entry(quotedKey: String, rawValue: String): Unit =
        if !first
        then sb.append(',')

        sb.append(quotedKey).append(':').append(rawValue): Unit
        first = false
      end entry

      specs.foreach { spec =>
        val v = fields.next()
        if !spec.policy.hidden
        then
          spec.policy.mask match
            case Some(mode) => entry(spec.quotedKey, JsonString.quoted(mode(spec.loggable.plain(v).value)).value)
            case None       =>
              val raw = spec.loggable.json(v).value
              if !spec.policy.unembedded
              then entry(spec.quotedKey, raw)
              else if raw.length >= 2 && raw.charAt(0) == '{' && raw.charAt(raw.length - 1) == '}'
              then
                val inner = raw.substring(1, raw.length - 1)
                if inner.nonEmpty
                then
                  if !first
                  then sb.append(',')
                  sb.append(inner)
                  first = false
              else entry(spec.quotedKey, raw)
      }

      JsonString(sb.append('}').toString)

  override def plain(a: A): PlainString =
    if specs.isEmpty
    then PlainString(typeName)
    else
      val fields   = a.asInstanceOf[Product].productIterator
      val rendered = List.newBuilder[(String, String)]

      specs.foreach { spec =>
        val v = fields.next()
        if !spec.policy.hidden
        then
          spec.policy.mask match
            case Some(mode) => rendered += spec.key -> mode(spec.loggable.plain(v).value)
            case None       => rendered += spec.key -> spec.loggable.plain(v).value
      }

      PlainString(cfg.plainValuesStyle.renderFields(rendered.result()))

final class SumLoggable[A](typeName: String, codecs: => List[Loggable[Any]], mirror: Mirror.SumOf[A]) extends Loggable[A]:
  private lazy val variants: Vector[Loggable[Any]] = codecs.toVector

  override val key: ValueKey            = ValueKey(decapitalize(typeName))
  override def json(a: A): JsonString   = variants(mirror.ordinal(a)).json(a)
  override def plain(a: A): PlainString = variants(mirror.ordinal(a)).plain(a)

final class EncodersLoggable[A](typeName: String, enc: JsonEncoder[A], plainEnc: PlainEncoder[A]) extends Loggable[A]:
  override val key: ValueKey            = ValueKey(typeName)
  override def json(a: A): JsonString   = enc.encode(a)
  override def plain(a: A): PlainString = plainEnc.encode(a)
