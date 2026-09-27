package logging4s.zio

import zio.prelude.Debug

import logging4s.core.{PlainEncoder, PlainString}

trait DebugToPlainEncoderInstance:

  given DebugPlainEncoder: [T] => (D: Debug[T]) => PlainEncoder[T] =
    (a: T) =>
      D.debug(a) match
        case Debug.Repr.String(value) => PlainString(value)
        case Debug.Repr.Char(value)   => PlainString(value.toString)
        case repr                     => PlainString(repr.render)

object DebugToPlainEncoderInstance extends DebugToPlainEncoderInstance
