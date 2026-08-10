package logging4s.core

object StructuredJson:

  def line(stringFields: Iterable[(String, String)], values: Iterable[LoggableValue]): String =
    val builder = new Builder
    builder.fields(stringFields)
    builder.values(values)
    builder.result

  final class Builder:
    private val sb    = new java.lang.StringBuilder(256).append('{')
    private var first = true

    private def separate(): Unit =
      if !first then sb.append(',')
      first = false

    def field(key: String, value: String): Unit =
      separate()
      sb.append(JsonString.quoted(key).value).append(':').append(JsonString.quoted(value).value)

    def fields(entries: Iterable[(String, String)]): Unit =
      entries.foreach((key, value) => field(key, value))

    def value(value: LoggableValue): Unit =
      separate()
      sb.append(JsonString.quoted(value.key.value).value).append(':').append(value.json.value)

    def values(values: Iterable[LoggableValue]): Unit =
      values.foreach(value)

    def result: String =
      sb.append('}').toString
