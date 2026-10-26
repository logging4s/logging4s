package logging4s.zio

trait ZioInstances extends DebugToPlainEncoderInstance, TaskToDelayInstance, UIOToDelayInstance, DataInstances, LocalContextInstance

object ZioInstances extends ZioInstances
