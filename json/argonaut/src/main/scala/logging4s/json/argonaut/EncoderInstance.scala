package logging4s.json.argonaut

import argonaut.EncodeJson

import logging4s.core.JsonEncoder

trait EncoderInstance:

  given [A: EncodeJson as E] => JsonEncoder[A] =
    a => E(a).nospaces
