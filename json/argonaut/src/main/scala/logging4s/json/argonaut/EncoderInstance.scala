package logging4s.json.argonaut

import argonaut.EncodeJson

import logging4s.core.{JsonEncoder, JsonString}

trait EncoderInstance:

  given ArgonautJsonEncoder[A](using E: EncodeJson[A]): JsonEncoder[A] =
    a => JsonString(E(a).nospaces)
