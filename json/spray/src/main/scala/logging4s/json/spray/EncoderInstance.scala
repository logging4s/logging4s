package logging4s.json.spray

import spray.json.JsonWriter

import logging4s.core.JsonEncoder

trait EncoderInstance:

  given [A](using W: JsonWriter[A]): JsonEncoder[A] =
    a => W.write(a).compactPrint
