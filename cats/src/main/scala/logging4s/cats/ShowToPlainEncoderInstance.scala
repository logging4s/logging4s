package logging4s.cats

import cats.Show
import logging4s.core.PlainEncoder

trait ShowToPlainEncoderInstance:

  given [T](using S: Show[T]): PlainEncoder[T] =
    (a: T) => S.show(a)

object ShowToPlainEncoderInstance extends ShowToPlainEncoderInstance