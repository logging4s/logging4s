package logging4s.json.play

import play.api.libs.json.Writes

import logging4s.core.JsonEncoder

trait EncoderInstance:

  given [A: Writes as W] => JsonEncoder[A] =
    a => W.writes(a).toString
