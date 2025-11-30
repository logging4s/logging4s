package logging4s.zio

trait instances extends DebugToPlainEncoderInstance with TaskToDelayInstance

object instances extends instances
