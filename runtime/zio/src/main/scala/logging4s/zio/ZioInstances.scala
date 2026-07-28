package logging4s.zio

trait ZioInstances extends DebugToPlainEncoderInstance with TaskToDelayInstance

object ZioInstances extends ZioInstances
