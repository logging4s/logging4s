package logging4s.json.fabric

import fabric.io.JsonFormatter
import fabric.rw.RW

import logging4s.core.JsonEncoder

import fabric.rw.given

trait EncoderInstance:

  given [A](using W: RW[A]): JsonEncoder[A] =
    a => JsonFormatter.Compact(a.json)
