package logging4s.core

import scala.deriving.Mirror

trait PlainEncoder[A]:
  def encode(a: A): PlainString

object PlainEncoder:
  inline def apply[A](using instance: PlainEncoder[A]): PlainEncoder[A] = instance

  inline def derived[A](using Mirror.Of[A]): PlainEncoder[A] =
    val loggable = Loggable.derived[A]
    (a: A) => loggable.plain(a)
