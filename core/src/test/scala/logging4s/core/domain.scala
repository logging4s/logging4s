package logging4s.core

import syntax.*

opaque type Str = String

object Str:
  def apply(str: String): Str = str

  given Loggable[Str] = Loggable[Str].rename("str")
