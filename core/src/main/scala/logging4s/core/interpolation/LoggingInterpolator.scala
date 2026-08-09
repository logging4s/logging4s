package logging4s.core.interpolation

import scala.quoted.*

import logging4s.core.{Loggable, LoggableValue, Logging, ValueKey}

private[core] object LoggingInterpolator:

  def infoImpl[F[*]: Type](sc: Expr[StringContext], args: Expr[Seq[Any]], logging: Expr[Logging[F]])(using
      Quotes
  ): Expr[F[Unit]] =
    build(sc, args, logging, "info")

  def warnImpl[F[*]: Type](sc: Expr[StringContext], args: Expr[Seq[Any]], logging: Expr[Logging[F]])(using
      Quotes
  ): Expr[F[Unit]] =
    build(sc, args, logging, "warn")

  def errorImpl[F[*]: Type](sc: Expr[StringContext], args: Expr[Seq[Any]], logging: Expr[Logging[F]])(using
      Quotes
  ): Expr[F[Unit]] =
    build(sc, args, logging, "error")

  def debugImpl[F[*]: Type](sc: Expr[StringContext], args: Expr[Seq[Any]], logging: Expr[Logging[F]])(using
      Quotes
  ): Expr[F[Unit]] =
    build(sc, args, logging, "debug")

  def traceImpl[F[*]: Type](sc: Expr[StringContext], args: Expr[Seq[Any]], logging: Expr[Logging[F]])(using
      Quotes
  ): Expr[F[Unit]] =
    build(sc, args, logging, "trace")

  private def build[F[*]: Type](
      sc: Expr[StringContext],
      args: Expr[Seq[Any]],
      logging: Expr[Logging[F]],
      level: String,
  )(using Quotes): Expr[F[Unit]] =
    import quotes.reflect.*

    val parts = sc match
      case '{ StringContext(${ Varargs(rawParts) }*) } => rawParts.map(_.valueOrAbort)
      case _                                           => report.errorAndAbort("the logging interpolator requires a string literal")

    val message = Expr(parts.mkString.replaceAll("[\\s:]+$", ""))

    val argExprs = args match
      case Varargs(exprs) => exprs
      case _              => report.errorAndAbort("the logging interpolator requires inline arguments")

    val values = Varargs(argExprs.map(toLoggableValue))

    level match
      case "info"  => '{ $logging.info($message, $values*) }
      case "warn"  => '{ $logging.warn($message, $values*) }
      case "error" => '{ $logging.error($message, $values*) }
      case "debug" => '{ $logging.debug($message, $values*) }
      case _       => '{ $logging.trace($message, $values*) }

  private def toLoggableValue(argExpr: Expr[Any])(using Quotes): Expr[LoggableValue] =
    import quotes.reflect.*

    def nameOf(term: Term): Option[String] =
      term match
        case Inlined(_, _, inner) => nameOf(inner)
        case Typed(inner, _)      => nameOf(inner)
        case Ident(name)          => Some(name)
        case Select(_, name)      => Some(name)
        case _                    => None

    val term = argExpr.asTerm
    val tpe  = term.tpe.widen

    if tpe <:< TypeRepr.of[LoggableValue] then argExpr.asExprOf[LoggableValue]
    else
      tpe.asType match
        case '[t] =>
          Expr.summon[Loggable[t]] match
            case Some(loggable) =>
              val arg = argExpr.asExprOf[t]
              nameOf(term) match
                case Some(name) =>
                  val key = Expr(name)
                  '{ val l = $loggable; LoggableValue(ValueKey($key), l.plain($arg), l.json($arg)) }
                case None       =>
                  '{ val l = $loggable; LoggableValue(l.key, l.plain($arg), l.json($arg)) }
            case None           =>
              report.errorAndAbort(s"no given Loggable[${tpe.show}] for the interpolated value")
