package logging4s.core

import scala.quoted.*

final case class Position(file: String, line: Int):
  def render: String = if line > 0 then s"$file:$line" else file

object Position:

  val unknown: Position = Position("<unknown>", 0)

  val Key: ValueKey = ValueKey("source")

  def asLogValue(position: Position): LoggableValue =
    val rendered = position.render
    LoggableValue(Key, PlainString(rendered), JsonString.quoted(rendered))

  inline given here: Position = ${ hereImpl }

  private def hereImpl(using Quotes): Expr[Position] =
    val position = quotes.reflect.Position.ofMacroExpansion
    ofExpansion(position.sourceFile.name, position.startLine + 1)

  private[core] def ofExpansion(file: String, line: Int)(using Quotes): Expr[Position] =
    '{ Position(${ Expr(file) }, ${ Expr(line) }) }
