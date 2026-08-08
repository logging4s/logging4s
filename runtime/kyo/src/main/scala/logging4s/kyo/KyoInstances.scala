package logging4s.kyo

trait KyoInstances extends IoToDelayInstance, DataInstances, RenderToPlainEncoderInstance

object KyoInstances extends KyoInstances
