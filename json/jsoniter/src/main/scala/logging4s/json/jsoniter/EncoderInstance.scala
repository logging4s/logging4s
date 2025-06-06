package logging4s.json.jsoniter

import com.github.plokhotnyuk.jsoniter_scala.core.JsonValueCodec
import com.github.plokhotnyuk.jsoniter_scala.core.writeToString

import logging4s.core.JsonEncoder

trait EncoderInstance:

  given [A: JsonValueCodec]: JsonEncoder[A] =
    a => writeToString(a)
