package logging4s.core.deriving.internal

import scala.deriving.Mirror

import logging4s.core.{JsonEncoder, JsonString, Loggable, LoggableValue, PlainEncoder, PlainString, ValueKey}
import logging4s.core.config.LoggableEncodingConfig
import logging4s.core.deriving.FieldPolicy

private[internal] def decapitalize(name: String): String =
  if name.isEmpty then name else s"${name.head.toLower}${name.tail}"

final class TupleLoggable[T <: Tuple](loggables: List[Loggable[Any]], cfg: LoggableEncodingConfig) extends Loggable[T]:

  override val key: ValueKey = ValueKey.combine(loggables.map(_.key)*)

  override def plain(t: T): PlainString =
    val elements = t.productIterator
    cfg.plainTupleStyle.render(loggables.map(_.plain(elements.next())))

  override def json(t: T): JsonString =
    val elements = t.productIterator
    if cfg.jsonTupleAsArray then JsonString.array(loggables.map(_.json(elements.next()))*)
    else JsonString.obj(loggables.map(l => l.key.value -> l.json(elements.next()))*)

final class ProductLoggable[A](
    typeName: String,
    labels: List[String],
    loggables: List[Loggable[Any]],
    policies: Map[String, FieldPolicy],
    cfg: LoggableEncodingConfig,
) extends Loggable[A]:

  override val key: ValueKey = ValueKey(decapitalize(typeName))

  private val specs: List[(Loggable[Any], String, Option[FieldPolicy])] =
    labels.lazyZip(loggables).map { (label, l) =>
      val policy   = policies.get(label)
      val fieldKey = policy match
        case Some(FieldPolicy.Rename(name)) => cfg.keyNameStyle.format(name)
        case _                              => cfg.keyNameStyle.format(label)
      (l, fieldKey, policy)
    }

  override def json(a: A): JsonString =
    if specs.isEmpty then JsonString.quoted(typeName)
    else
      val fields = a.asInstanceOf[Product].productIterator
      val sb     = new StringBuilder("{")
      var first  = true

      def entry(fieldKey: String, rawValue: String): Unit =
        if !first then sb.append(',')
        sb.append('"').append(fieldKey).append("\":").append(rawValue)
        first = false

      specs.foreach { (l, fieldKey, policy) =>
        val v = fields.next()
        policy match
          case Some(FieldPolicy.Hide)       => ()
          case Some(FieldPolicy.Mask(mode)) => entry(fieldKey, JsonString.quoted(mode(l.plain(v).value)).value)
          case Some(FieldPolicy.Unembed)    =>
            val raw = l.json(v).value
            if raw.length >= 2 && raw.charAt(0) == '{' && raw.charAt(raw.length - 1) == '}' then
              val inner = raw.substring(1, raw.length - 1)
              if inner.nonEmpty then
                if !first then sb.append(',')
                sb.append(inner)
                first = false
            else entry(fieldKey, raw)
          case _                            => entry(fieldKey, l.json(v).value)
      }

      JsonString(sb.append('}').toString)

  override def plain(a: A): PlainString =
    if specs.isEmpty then PlainString(typeName)
    else
      val fields   = a.asInstanceOf[Product].productIterator
      val rendered = List.newBuilder[(String, String)]

      specs.foreach { (l, fieldKey, policy) =>
        val v = fields.next()
        policy match
          case Some(FieldPolicy.Hide)       => ()
          case Some(FieldPolicy.Mask(mode)) => rendered += fieldKey -> mode(l.plain(v).value)
          case _                            => rendered += fieldKey -> l.plain(v).value
      }

      PlainString(cfg.plainValuesStyle.renderFields(rendered.result()))

final class SumLoggable[A](typeName: String, loggables: List[Loggable[Any]], mirror: Mirror.SumOf[A]) extends Loggable[A]:
  private val variants: Vector[Loggable[Any]] = loggables.toVector

  override val key: ValueKey            = ValueKey(decapitalize(typeName))
  override def json(a: A): JsonString   = variants(mirror.ordinal(a)).json(a)
  override def plain(a: A): PlainString = variants(mirror.ordinal(a)).plain(a)

final class EncodersLoggable[A](typeName: String, enc: JsonEncoder[A], plainEnc: PlainEncoder[A]) extends Loggable[A]:
  override val key: ValueKey            = ValueKey(typeName)
  override def json(a: A): JsonString   = enc.encode(a)
  override def plain(a: A): PlainString = plainEnc.encode(a)
