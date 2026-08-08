package logging4s.kyo

import kyo.Render

import logging4s.core.{PlainEncoder, PlainString}

trait RenderToPlainEncoderInstance:

  given RenderPlainEncoder: [T: Render as R] => PlainEncoder[T] =
    a => PlainString(R.asString(a))

object RenderToPlainEncoderInstance extends RenderToPlainEncoderInstance
