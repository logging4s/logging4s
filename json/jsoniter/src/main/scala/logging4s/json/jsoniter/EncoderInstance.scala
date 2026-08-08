package logging4s.json.jsoniter

import com.github.plokhotnyuk.jsoniter_scala.core.JsonValueCodec
import com.github.plokhotnyuk.jsoniter_scala.core.writeToString

import logging4s.core.{JsonEncoder, JsonString}

trait EncoderInstance:

  given JsoniterJsonEncoder[A: JsonValueCodec]: JsonEncoder[A] =
    a => JsonString(writeToString(a))
