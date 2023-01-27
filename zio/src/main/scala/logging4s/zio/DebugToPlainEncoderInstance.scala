package logging4s.zio

import zio.prelude.Debug
import logging4s.core.PlainEncoder

trait DebugToPlainEncoderInstance:

  given [T](using D: Debug[T]): PlainEncoder[T] =
    (a: T) => D.render(a)

object DebugToPlainEncoderInstance extends DebugToPlainEncoderInstance
