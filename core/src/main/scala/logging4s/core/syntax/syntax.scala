package logging4s.core.syntax

trait AllSyntax extends LoggableSyntax, LoggingSyntax

object loggable extends LoggableSyntax
object logging  extends LoggingSyntax
object all      extends AllSyntax
