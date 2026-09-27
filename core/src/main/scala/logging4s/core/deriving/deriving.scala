package logging4s.core.deriving

import scala.deriving.Mirror
import scala.compiletime.summonInline

import logging4s.core.Loggable
import logging4s.core.config.LoggableEncodingConfig

enum MaskMode:
  case Full
  case KeepLast(visible: Int)
  case KeepFirst(visible: Int)

  def apply(value: String): String =
    this match
      case Full         => "*" * value.length
      case KeepLast(n)  => if value.length <= n then value else "*" * (value.length - n) + value.takeRight(n)
      case KeepFirst(n) => if value.length <= n then value else value.take(n) + "*" * (value.length - n)

final case class FieldPolicy(
    name: Option[String] = None,
    mask: Option[MaskMode] = None,
    hidden: Boolean = false,
    unembedded: Boolean = false,
)

object FieldPolicy:
  val none: FieldPolicy = FieldPolicy()

final class LoggableBuilder[A](val policies: Map[String, FieldPolicy], val key: Option[String]):

  inline def hide(inline selector: A => Any): LoggableBuilder[A] =
    updated(macros.fieldName(selector))(_.copy(hidden = true))

  inline def mask(inline selector: A => Any)(mode: MaskMode): LoggableBuilder[A] =
    updated(macros.fieldName(selector))(_.copy(mask = Some(mode)))

  inline def rename(inline selector: A => Any, name: String): LoggableBuilder[A] =
    updated(macros.fieldName(selector))(_.copy(name = Some(name)))

  inline def unembed(inline selector: A => Any): LoggableBuilder[A] =
    updated(macros.fieldName(selector))(_.copy(unembedded = true))

  private def updated(field: String)(change: FieldPolicy => FieldPolicy): LoggableBuilder[A] =
    LoggableBuilder(policies.updated(field, change(policies.getOrElse(field, FieldPolicy.none))), key)

  inline def derived(using m: Mirror.ProductOf[A]): Loggable[A] =
    val base = macros.deriveProductWith[A](policies)(using m, summonInline[LoggableEncodingConfig])
    key.fold(base)(base.rename)
