package logging4s.core

trait JsonEncoder[A]:
  def encode(a: A): JsonString

object JsonEncoder:
  inline def apply[A](using instance: JsonEncoder[A]): JsonEncoder[A] = instance
