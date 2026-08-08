package logging4s.cats

import cats.Show

import logging4s.core.{PlainEncoder, PlainString}

trait ShowToPlainEncoderInstance:

  given ShowPlainEncoder[T](using S: Show[T]): PlainEncoder[T] =
    (a: T) => PlainString(S.show(a))

object ShowToPlainEncoderInstance extends ShowToPlainEncoderInstance
