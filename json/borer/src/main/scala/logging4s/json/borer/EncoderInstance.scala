package logging4s.json.borer

import io.bullet.borer.{Encoder, Json}

import logging4s.core.JsonEncoder

trait EncoderInstance:

  given [A: Encoder] => JsonEncoder[A] =
    a => Json.encode(a).toUtf8String
