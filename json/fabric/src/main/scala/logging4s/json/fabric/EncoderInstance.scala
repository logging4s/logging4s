package logging4s.json.fabric

import fabric.rw.RW
import fabric.io.JsonFormatter

import logging4s.core.JsonEncoder

import fabric.rw.given

trait EncoderInstance:

  given [A: RW]: JsonEncoder[A] =
    a => JsonFormatter.Compact(a.asJson)
