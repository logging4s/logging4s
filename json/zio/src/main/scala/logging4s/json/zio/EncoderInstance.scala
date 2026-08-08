package logging4s.json.zio

import logging4s.core.{JsonEncoder, JsonString}

import zio.json.JsonEncoder as Encoder

trait EncoderInstance:

  given ZioJsonEncoder[A](using E: Encoder[A]): JsonEncoder[A] =
    a => JsonString(E.encodeJson(a).toString)
