package logging4s.json.weepickle

import com.rallyhealth.weejson.v1.jackson.ToJson
import com.rallyhealth.weepickle.v1.WeePickle.{FromScala, From}

import logging4s.core.{JsonEncoder, JsonString}

trait EncoderInstance:

  given WeepickleJsonEncoder[A: From]: JsonEncoder[A] =
    a => JsonString(FromScala(a).transform(ToJson.string))
