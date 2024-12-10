package logging4s.json.weepickle

import com.rallyhealth.weejson.v1.jackson.ToJson
import com.rallyhealth.weepickle.v1.WeePickle.{FromScala, From}

import logging4s.core.JsonEncoder

trait EncoderInstance:

  given [A](using From[A]): JsonEncoder[A] =
    a => FromScala(a).transform(ToJson.string)
