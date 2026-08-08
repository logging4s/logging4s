package logging4s.zio

trait ZioInstances extends DebugToPlainEncoderInstance, TaskToDelayInstance

object ZioInstances extends ZioInstances
