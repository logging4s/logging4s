package logging4s.console

import com.typesafe.config.{Config, ConfigFactory}

enum Format:
  case Json, Plain

object Format:
  def parse(raw: String): Format =
    values
      .find(_.toString.equalsIgnoreCase(raw.trim))
      .getOrElse(throw new IllegalArgumentException(s"Invalid logging4s.console.format: '$raw'"))

enum ColorMode:
  case Auto, On, Off

object ColorMode:
  def parse(raw: String): ColorMode =
    values
      .find(_.toString.equalsIgnoreCase(raw.trim))
      .getOrElse(throw new IllegalArgumentException(s"Invalid logging4s.console.color: '$raw'"))

enum Stream:
  case Stdout, Stderr

object Stream:
  def parse(raw: String): Stream =
    values
      .find(_.toString.equalsIgnoreCase(raw.trim))
      .getOrElse(throw new IllegalArgumentException(s"Invalid logging4s.console.stream: '$raw'"))

final case class ConsoleConfig(
    level: Level,
    format: Format,
    color: ColorMode,
    stream: Stream,
    maxStackTraceLines: Int,
)

object ConsoleConfig:

  private def load(config: Config = ConfigFactory.load()): ConsoleConfig =
    val section = config.getConfig("logging4s.console")
    ConsoleConfig(
      level = Level.parse(section.getString("level")),
      format = Format.parse(section.getString("format")),
      color = ColorMode.parse(section.getString("color")),
      stream = Stream.parse(section.getString("stream")),
      maxStackTraceLines = section.getInt("max-stack-trace-lines"),
    )

  given default: ConsoleConfig = load()
