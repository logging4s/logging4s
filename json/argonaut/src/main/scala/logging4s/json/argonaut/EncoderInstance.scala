package logging4s.json.argonaut

import argonaut.EncodeJson

import logging4s.core.JsonEncoder

trait EncoderInstance:

  given [A](using E: EncodeJson[A]): JsonEncoder[A] =
    a => E(a).nospaces
