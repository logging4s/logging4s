package logging4s.json.fabric

import fabric.rw.RW
import fabric.io.JsonFormatter

import logging4s.core.{JsonEncoder, JsonString}

import fabric.rw.given

trait EncoderInstance:

  given FabricJsonEncoder[A: RW]: JsonEncoder[A] =
    a => JsonString(JsonFormatter.Compact(a.asJson))
