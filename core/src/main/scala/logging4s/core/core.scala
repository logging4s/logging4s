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

    def pluralized: ValueKey =
      if v.length > 1 && v.endsWith("y") && !"aeiou".contains(v.charAt(v.length - 2)) then s"${v.dropRight(1)}ies"
      else if v.endsWith("s") || v.endsWith("x") || v.endsWith("z") || v.endsWith("ch") || v.endsWith("sh") then s"${v}es"
      else s"${v}s"

opaque type JsonString = String
object JsonString:
  inline def apply(src: String): JsonString = src
  def quoted(raw: String): JsonString       = s"\"${escape(raw)}\""

  private def needsEscape(c: Char): Boolean =
    c == '"' || c == '\\' || c < 0x20

  private def escape(raw: String): String =
    val n = raw.length
    var i = 0
    while i < n && !needsEscape(raw.charAt(i)) do i += 1
    if i == n then raw
    else
      val sb = new java.lang.StringBuilder(n + 8)
      sb.append(raw, 0, i)
      while i < n do
        val c = raw.charAt(i)
        c match
          case '"'  => sb.append("\\\"")
          case '\\' => sb.append("\\\\")
          case '\n' => sb.append("\\n")
          case '\r' => sb.append("\\r")
          case '\t' => sb.append("\\t")
          case '\b' => sb.append("\\b")
          case '\f' => sb.append("\\f")
          case _    => if c < 0x20 then sb.append("\\u%04x".format(c.toInt)) else sb.append(c)
        i += 1
      sb.toString

  def array(elements: JsonString*): JsonString = elements.mkString("[", ",", "]")

  def obj(fields: (String, JsonString)*): JsonString =
    fields.map((key, value) => s"\"$key\":${value.value}").mkString("{", ",", "}")

  extension (v: JsonString) inline def value: String = v

opaque type PlainString = String
object PlainString:
  inline def apply(src: String): PlainString = src

  def array(elements: PlainString*): PlainString = elements.mkString("[", ",", "]")

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
