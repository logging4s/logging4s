package logging4s.kyo

import kyo.{Sync, <}

type KIO[T] = T < Sync
