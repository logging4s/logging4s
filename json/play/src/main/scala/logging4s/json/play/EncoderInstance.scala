package logging4s.json.play

import play.api.libs.json.Writes

import logging4s.core.{JsonEncoder, JsonString}

trait EncoderInstance:

  given PlayJsonEncoder[A](using W: Writes[A]): JsonEncoder[A] =
    a => JsonString(W.writes(a).toString)
