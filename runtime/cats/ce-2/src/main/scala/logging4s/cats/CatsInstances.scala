package logging4s.cats

trait CatsInstances extends ShowToPlainEncoderInstance with SyncToDelayInstance

object CatsInstances extends CatsInstances
