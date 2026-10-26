package logging4s.core

import scala.quoted.*

import logging4s.core.config.LoggableEncodingConfig
import logging4s.core.syntax.all.plain

object LogMessage:

  inline def static(inline message: String): String = ${ staticImpl('message) }

  private def staticImpl(message: Expr[String])(using Quotes): Expr[String] =
    import quotes.reflect.*

    def interpolation(term: Term): Boolean =
      term match
        case Inlined(_, _, inner)                                       => interpolation(inner)
        case Typed(inner, _)                                            => interpolation(inner)
        case Block(Nil, inner)                                          => interpolation(inner)
        case Apply(Select(receiver, name), args) if isContext(receiver) => isInterpolator(name) && holes(args)
        case _                                                          => false

    def isContext(term: Term): Boolean = term.tpe.widen <:< TypeRepr.of[StringContext]

    def isInterpolator(name: String): Boolean = name == "s" || name == "f" || name == "raw"

    def holes(args: List[Term]): Boolean =
      args.exists {
        case Typed(Repeated(values, _), _) => values.nonEmpty
        case Repeated(values, _)           => values.nonEmpty
        case _                             => true
      }

    if !interpolation(message.asTerm)
    then message
    else
      report.errorAndAbort(
        "a log message must stay static text, so that it can be used as an aggregation key. "
          + "Pass the interpolated values as values instead — log.info(\"user created\", user) — "
          + "or use the info\"user created $user\" interpolator, which extracts them for you."
      )

  def render(message: String, cause: Option[Throwable], values: Seq[LoggableValue])(using cfg: LoggableEncodingConfig): String =
    renderAll(message, cause, if cfg.includeValuesInMessage then values else Nil)

  def renderAll(message: String, cause: Option[Throwable], values: Seq[LoggableValue])(using LoggableEncodingConfig): String =
    cause match
      case None        => if values.isEmpty then message else s"$message: ${sanitize(values.plain)}"
      case Some(error) =>
        val base = s"$message: class=${error.getClass.getName}, message=${sanitize(String.valueOf(error.getMessage))}"
        if values.isEmpty then base else s"$base, ${sanitize(values.plain)}"

  private def unsafe(c: Char): Boolean =
    (c < 0x20 && c != '\t') || c == 0x7f || c == 0x85 || c == 0x2028 || c == 0x2029

  private[core] def sanitize(text: String): String =
    val n = text.length
    var i = 0
    while i < n && !unsafe(text.charAt(i)) do i += 1
    if i == n then text
    else
      val sb = new java.lang.StringBuilder(n + 8)
      sb.append(text, 0, i)
      while i < n do
        val c = text.charAt(i)
        c match
          case '\n'           => sb.append("\\n")
          case '\r'           => sb.append("\\r")
          case _ if unsafe(c) => sb.append("\\u%04x".format(c.toInt))
          case _              => sb.append(c)
        i += 1
      sb.toString
  end sanitize
