package logging4s.console

enum Level(val priority: Int):
  case Error extends Level(5)
  case Warn  extends Level(4)
  case Info  extends Level(3)
  case Debug extends Level(2)
  case Trace extends Level(1)

  def enabledAt(threshold: Level): Boolean = priority >= threshold.priority

object Level:
  def parse(raw: String): Level =
    values
      .find(_.toString.equalsIgnoreCase(raw.trim))
      .getOrElse(throw new IllegalArgumentException(s"Invalid logging4s.console.level: '$raw'"))
