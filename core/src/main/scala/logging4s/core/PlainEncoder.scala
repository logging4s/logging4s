package logging4s.core

trait PlainEncoder[A]:
  def encode(a: A): String

object PlainEncoder:
  inline def apply[A](using instance: PlainEncoder[A]): PlainEncoder[A] = instance
