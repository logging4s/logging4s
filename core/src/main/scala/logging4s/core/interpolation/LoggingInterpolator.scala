package logging4s.core.interpolation

import scala.quoted.*

import logging4s.core.{Level, Loggable, LoggableValue, Logging, ValueKey}

private[core] object LoggingInterpolator:

  def infoImpl[F[*]: Type](
      sc: Expr[StringContext],
      args: Expr[Seq[Any]],
      logging: Expr[Logging[F]],
  )(using Quotes): Expr[F[Unit]] =
    build(sc, args, logging, "info")

  def warnImpl[F[*]: Type](
      sc: Expr[StringContext],
      args: Expr[Seq[Any]],
      logging: Expr[Logging[F]],
  )(using Quotes): Expr[F[Unit]] =
    build(sc, args, logging, "warn")

  def errorImpl[F[*]: Type](
      sc: Expr[StringContext],
      args: Expr[Seq[Any]],
      logging: Expr[Logging[F]],
  )(using Quotes): Expr[F[Unit]] =
    build(sc, args, logging, "error")

  def debugImpl[F[*]: Type](
      sc: Expr[StringContext],
      args: Expr[Seq[Any]],
      logging: Expr[Logging[F]],
  )(using Quotes): Expr[F[Unit]] =
    build(sc, args, logging, "debug")

  def traceImpl[F[*]: Type](
      sc: Expr[StringContext],
      args: Expr[Seq[Any]],
      logging: Expr[Logging[F]],
  )(using Quotes): Expr[F[Unit]] =
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

    val message = Expr(joinParts(parts))

    val argExprs = args match
      case Varargs(exprs) => exprs
      case _              => report.errorAndAbort("the logging interpolator requires inline arguments")

    val values = Expr.ofSeq(argExprs.map(toLoggableValue))

    val levelExpr = level match
      case "info"  => '{ Level.Info }
      case "warn"  => '{ Level.Warn }
      case "error" => '{ Level.Error }
      case "debug" => '{ Level.Debug }
      case _       => '{ Level.Trace }

    val expansion = quotes.reflect.Position.ofMacroExpansion
    val position  = logging4s.core.Position.ofExpansion(expansion.sourceFile.name, expansion.startLine + 1)

    '{
      val logger = $logging
      if logger.enabled($levelExpr) then logger.emit($levelExpr, $message, None, $values)(using $position) else logger.unit
    }

  private def joinParts(parts: Seq[String]): String =
    parts
      .foldLeft(new StringBuilder) { (acc, part) =>
        if acc.nonEmpty && acc.last.isWhitespace && part.headOption.exists(_.isWhitespace)
        then acc.append(part.stripLeading)
        else acc.append(part)
      }
      .toString
      .replaceAll("[\\s:]+$", "")

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
                  '{ val l = $loggable; LoggableValue.deferred(ValueKey($key), $arg, l) }
                case None       =>
                  '{ val l = $loggable; LoggableValue.deferred(l.key, $arg, l) }
            case None           =>
              report.errorAndAbort(s"no given Loggable[${tpe.show}] for the interpolated value")
