package logging4s.json.spray

import spray.json.JsonWriter

import logging4s.core.JsonEncoder

trait EncoderInstance:

  given [A](using F: JsonWriter[A]): JsonEncoder[A] =
    a => F.write(a).compactPrint
