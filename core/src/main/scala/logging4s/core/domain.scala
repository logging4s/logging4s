package logging4s.core

import scala.annotation.targetName

type Identity[A] = A

type ThrowableEither[A] = Either[Throwable, A]

opaque type ValueKey = String
object ValueKey:
  inline def apply(src: String): ValueKey = src

  def combine(keys: ValueKey*): ValueKey =
    if keys.forall(_ == keys.head)
    then keys.head
    else keys.mkString("_")

  extension (v: ValueKey)
    inline def value: String           = v
    def suffixed(index: Int): ValueKey = s"${v}_$index"

opaque type JsonString = String
object JsonString:
  inline def apply(src: String): JsonString  = src
  inline def quoted(raw: String): JsonString = s"\"$raw\""

  def array(elements: JsonString*): JsonString = elements.mkString("[", ",", "]")

  extension (v: JsonString) inline def value: String = v

opaque type PlainString = String
object PlainString:
  inline def apply(src: String): PlainString = src

  def array(elements: PlainString*): PlainString = elements.mkString("[", ",", "]")

  def tuple(elements: PlainString*): PlainString = elements.mkString("(", ", ", ")")

  extension (v: PlainString) inline def value: String = v

opaque type LoggingContext = Seq[LoggableValue]
object LoggingContext:
  val empty: LoggingContext = LoggingContext(Seq.empty)

  inline def apply(values: Seq[LoggableValue]): LoggingContext = values

  @targetName("applyValue")
  inline def apply(values: LoggableValue*): LoggingContext = values.toSeq

  extension (that: LoggingContext)
    inline def values: Seq[LoggableValue] = that

    @targetName("plus")
    inline infix def +(other: LoggingContext): LoggingContext = that ++ other
