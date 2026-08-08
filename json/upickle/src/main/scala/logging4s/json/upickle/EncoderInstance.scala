package logging4s.json.upickle

import upickle.default.{Writer, write}
import upickle.*

import logging4s.core.{JsonEncoder, JsonString}

trait EncoderInstance:

  given UpickleJsonEncoder[A: Writer]: JsonEncoder[A] =
    a => JsonString(write(a))
