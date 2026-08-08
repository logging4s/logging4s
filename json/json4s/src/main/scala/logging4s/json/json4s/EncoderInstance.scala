package logging4s.json.json4s

import logging4s.core.{JsonEncoder, JsonString}

import org.json4s.*
import org.json4s.native.Serialization

trait EncoderInstance:

  given Json4sJsonEncoder[A](using Formats): JsonEncoder[A] =
    a => JsonString(Serialization.write(a))
