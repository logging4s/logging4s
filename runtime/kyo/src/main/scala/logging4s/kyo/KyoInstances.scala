package logging4s.kyo

trait KyoInstances extends SyncToDelayInstance, DataInstances, RenderToPlainEncoderInstance, LocalContextInstance

object KyoInstances extends KyoInstances
