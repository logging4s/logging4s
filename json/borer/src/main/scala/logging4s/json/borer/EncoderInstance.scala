package logging4s.json.borer

import io.bullet.borer.{Encoder, Json}

import logging4s.core.{JsonEncoder, JsonString}

trait EncoderInstance:

  given BorerJsonEncoder[A: Encoder]: JsonEncoder[A] =
    a => JsonString(Json.encode(a).toUtf8String)
