package logging4s.kyo

import kyo.Render

import logging4s.core.PlainEncoder

trait RenderToPlainEncoderInstance:

  given [T: Render as R] => PlainEncoder[T] =
    a => R.asString(a)

object RenderToPlainEncoderInstance extends RenderToPlainEncoderInstance
