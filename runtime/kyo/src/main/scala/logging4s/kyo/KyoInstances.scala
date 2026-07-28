package logging4s.kyo

trait KyoInstances extends IoToDelayInstance with DataInstances with RenderToPlainEncoderInstance

object KyoInstances extends KyoInstances
