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

enum FieldPolicy:
  case Hide
  case Mask(mode: MaskMode)
  case Rename(name: String)
  case Unembed

final class LoggableBuilder[A](val policies: Map[String, FieldPolicy], val key: Option[String]):

  inline def hide(inline selector: A => Any): LoggableBuilder[A] =
    LoggableBuilder(policies.updated(macros.fieldName(selector), FieldPolicy.Hide), key)

  inline def mask(inline selector: A => Any)(mode: MaskMode): LoggableBuilder[A] =
    LoggableBuilder(policies.updated(macros.fieldName(selector), FieldPolicy.Mask(mode)), key)

  inline def rename(inline selector: A => Any, name: String): LoggableBuilder[A] =
    LoggableBuilder(policies.updated(macros.fieldName(selector), FieldPolicy.Rename(name)), key)

  inline def unembed(inline selector: A => Any): LoggableBuilder[A] =
    LoggableBuilder(policies.updated(macros.fieldName(selector), FieldPolicy.Unembed), key)

  inline def derived(using m: Mirror.ProductOf[A]): Loggable[A] =
    val base = macros.deriveProductWith[A](policies)(using m, summonInline[LoggableEncodingConfig])
    key.fold(base)(base.rename)
