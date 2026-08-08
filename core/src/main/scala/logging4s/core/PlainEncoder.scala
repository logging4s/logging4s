package logging4s.core

trait PlainEncoder[A]:
  def encode(a: A): PlainString

object PlainEncoder:
  inline def apply[A](using instance: PlainEncoder[A]): PlainEncoder[A] = instance
