package logging4s.kyo

import kyo.{IO, <}

type KIO[T] = T < IO
