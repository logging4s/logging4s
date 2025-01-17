package logging4s.json.upickle

import upickle.default.{Writer, write}
import upickle.*

import logging4s.core.JsonEncoder

trait EncoderInstance:

  given [A: Writer] => JsonEncoder[A] =
    a => write(a)
