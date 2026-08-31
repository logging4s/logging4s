package logging4s.kyo

trait KyoInstances extends SyncToDelayInstance, DataInstances, RenderToPlainEncoderInstance

object KyoInstances extends KyoInstances
