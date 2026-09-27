package logging4s.core.conformance

object JsonRecord:

  def parse(line: String): CapturedRecord =
    val entries = topLevelEntries(line.trim.stripPrefix("{").stripSuffix("}"))
    val message = entries.collectFirst { case ("message", value) => unquote(value) }.getOrElse("")

    CapturedRecord(message, entries)

  private def topLevelEntries(body: String): List[(String, String)] =
    val entries = List.newBuilder[(String, String)]

    var index    = 0
    var start    = 0
    var depth    = 0
    var inString = false
    var escaped  = false

    while index < body.length do
      val char = body.charAt(index)

      if escaped then escaped = false
      else if inString then
        if char == '\\' then escaped = true
        else if char == '"' then inString = false
      else
        char match
          case '"'               => inString = true
          case '{' | '['         => depth += 1
          case '}' | ']'         => depth -= 1
          case ',' if depth == 0 =>
            entries += entry(body.substring(start, index))
            start = index + 1
          case _                 => ()

      index += 1

    if start < body.length then entries += entry(body.substring(start))

    entries.result()
  end topLevelEntries

  private def entry(raw: String): (String, String) =
    val separator = colonOutsideString(raw)

    if separator < 0
    then (unquote(raw.trim), "")
    else (unquote(raw.take(separator).trim), raw.drop(separator + 1).trim)

  private def colonOutsideString(raw: String): Int =
    var index    = 0
    var inString = false
    var escaped  = false
    var found    = -1

    while index < raw.length && found < 0 do
      val char = raw.charAt(index)

      if escaped then escaped = false
      else if inString then
        if char == '\\' then escaped = true
        else if char == '"' then inString = false
      else if char == '"' then inString = true
      else if char == ':' then found = index

      index += 1

    found

  private def unquote(raw: String): String =
    if raw.length >= 2 && raw.startsWith("\"") && raw.endsWith("\"")
    then raw.substring(1, raw.length - 1).replace("\\\"", "\"").replace("\\\\", "\\")
    else raw
