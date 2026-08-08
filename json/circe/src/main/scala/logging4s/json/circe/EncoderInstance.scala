package logging4s.json.circe

import io.circe.Encoder

import logging4s.core.{JsonEncoder, JsonString}

trait EncoderInstance:

  given CirceJsonEncoder[A](using E: Encoder[A]): JsonEncoder[A] =
    a => JsonString(E(a).noSpaces)
