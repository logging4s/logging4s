package logging4s.zio

import zio.prelude.Debug
import logging4s.core.{PlainEncoder, PlainString}

trait DebugToPlainEncoderInstance:

  given DebugPlainEncoder[T](using D: Debug[T]): PlainEncoder[T] =
    (a: T) =>
      val rendered = D.render(a)
      PlainString(rendered.substring(1, rendered.length() - 1))

object DebugToPlainEncoderInstance extends DebugToPlainEncoderInstance
