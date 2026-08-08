package logging4s.json.spray

import spray.json.JsonWriter

import logging4s.core.{JsonEncoder, JsonString}

trait EncoderInstance:

  given SprayJsonEncoder[A](using W: JsonWriter[A]): JsonEncoder[A] =
    a => JsonString(W.write(a).compactPrint)
