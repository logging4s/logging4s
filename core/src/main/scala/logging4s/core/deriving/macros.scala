package logging4s.core.deriving

import scala.deriving.Mirror
import scala.compiletime.{constValue, erasedValue, summonAll, summonFrom, summonInline}
import scala.quoted.*

import logging4s.core.{JsonEncoder, Loggable, PlainEncoder, PlainString}
import logging4s.core.config.LoggableEncodingConfig
import logging4s.core.deriving.internal.*

private[core] object macros:

  inline def deriveTuple[T <: Tuple](using cfg: LoggableEncodingConfig): Loggable[T] =
    TupleLoggable[T](summonAll[Tuple.Map[T, Loggable]].toList.asInstanceOf[List[Loggable[Any]]], cfg)

  inline def derived[A](using m: Mirror.Of[A]): Loggable[A] =
    inline m match
      case p: Mirror.ProductOf[A] => deriveProduct[A](using p, summonInline[LoggableEncodingConfig])
      case s: Mirror.SumOf[A]     => deriveSum[A](using s)

  inline def fromEncoders[A](using enc: JsonEncoder[A]): Loggable[A] = fromEncoders[A](typeName[A])

  inline def fromEncoders[A](key: String)(using enc: JsonEncoder[A]): Loggable[A] =
    val plainEnc: PlainEncoder[A] =
      summonFrom {
        case pe: PlainEncoder[A] => pe
        case given Mirror.Of[A]  => PlainEncoder.derived[A]
        case _                   => (a: A) => PlainString(enc.encode(a).value)
      }

    EncodersLoggable[A](key, enc, plainEnc)

  inline def deriveProduct[A](using p: Mirror.ProductOf[A], cfg: LoggableEncodingConfig): Loggable[A] =
    deriveProductWith[A](Map.empty)

  inline def deriveProductWith[A](
      policies: Map[String, FieldPolicy]
  )(using p: Mirror.ProductOf[A], cfg: LoggableEncodingConfig): Loggable[A] =
    ProductLoggable[A](
      constValue[p.MirroredLabel],
      elemLabels[p.MirroredElemLabels],
      summonAll[Tuple.Map[p.MirroredElemTypes, Loggable]].toList.asInstanceOf[List[Loggable[Any]]],
      policies,
      cfg,
    )

  inline def deriveSum[A](using s: Mirror.SumOf[A]): Loggable[A] =
    SumLoggable[A](constValue[s.MirroredLabel], summonOrDeriveAll[s.MirroredElemTypes], s)

  private inline def elemLabels[T <: Tuple]: List[String] =
    inline erasedValue[T] match
      case _: EmptyTuple => Nil
      case _: (h *: t)   => constValue[h].toString :: elemLabels[t]

  private inline def summonOrDeriveAll[T <: Tuple]: List[Loggable[Any]] =
    inline erasedValue[T] match
      case _: EmptyTuple => Nil
      case _: (h *: t)   => summonOrDerive[h] :: summonOrDeriveAll[t]

  private inline def summonOrDerive[A]: Loggable[Any] =
    summonFrom {
      case l: Loggable[A]  => l.asInstanceOf[Loggable[Any]]
      case m: Mirror.Of[A] => derived[A](using m).asInstanceOf[Loggable[Any]]
    }

  inline def typeName[A]: String = ${ typeNameImpl[A] }

  private def typeNameImpl[A: Type](using Quotes): Expr[String] =
    import quotes.reflect.*
    val name = TypeRepr.of[A].typeSymbol.name
    Expr(if name.isEmpty then name else s"${name.head.toLower}${name.tail}")

  inline def fieldName[A](inline selector: A => Any): String = ${ fieldNameImpl('selector) }

  private def fieldNameImpl[A: Type](selector: Expr[A => Any])(using Quotes): Expr[String] =
    import quotes.reflect.*

    def unwrap(term: Term): Term =
      term match
        case Inlined(_, _, inner) => unwrap(inner)
        case Block(Nil, inner)    => unwrap(inner)
        case Typed(inner, _)      => unwrap(inner)
        case other                => other

    unwrap(selector.asTerm) match
      case Lambda(List(param), body) =>
        unwrap(body) match
          case Select(Ident(name), field) if name == param.name => Expr(field)
          case other                                            => report.errorAndAbort(s"Expected a direct field selector like _.field, but got: ${other.show}")
      case other                     => report.errorAndAbort(s"Expected a lambda field selector like _.field, but got: ${other.show}")
