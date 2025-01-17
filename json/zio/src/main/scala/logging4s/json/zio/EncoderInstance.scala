package logging4s.json.zio

import logging4s.core.JsonEncoder

import zio.json.JsonEncoder as Encoder

trait EncoderInstance:

  given [A: Encoder as E] => JsonEncoder[A] =
    a => E.encodeJson(a).toString
