package logging4s.json.json4s

import logging4s.core.JsonEncoder

import org.json4s.*
import org.json4s.native.Serialization

trait EncoderInstance:

  given [A] => Formats => JsonEncoder[A] =
    a => Serialization.write(a)
