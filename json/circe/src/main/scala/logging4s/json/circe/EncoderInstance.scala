package logging4s.json.circe

import io.circe.Encoder

import logging4s.core.JsonEncoder

trait EncoderInstance:

  given [A : Encoder as E] => JsonEncoder[A] = 
    a => E(a).noSpaces
