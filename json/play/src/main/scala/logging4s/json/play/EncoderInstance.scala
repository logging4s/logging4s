package logging4s.json.play

import play.api.libs.json.Writes

import logging4s.core.JsonEncoder

trait EncoderInstance:

  given [A](using W: Writes[A]): JsonEncoder[A] =
    a => W.writes(a).toString
