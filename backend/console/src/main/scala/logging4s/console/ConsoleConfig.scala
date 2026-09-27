package logging4s.console

import com.typesafe.config.{Config, ConfigFactory, ConfigUtil}

import logging4s.core.Level

import scala.jdk.CollectionConverters.given

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

enum Threshold:
  case Off
  case At(level: Level)

  def allows(level: Level): Boolean =
    this match
      case Off         => false
      case At(minimum) => level.enabledAt(minimum)

object Threshold:
  def parse(raw: String): Threshold =
    if raw.trim.equalsIgnoreCase("off")
    then Off
    else Level.parse(raw).map(At.apply).getOrElse(throw new IllegalArgumentException(s"Invalid logging4s.console.level: '$raw'"))

final case class ConsoleConfig(
    level: Threshold,
    format: Format,
    color: ColorMode,
    stream: Stream,
    maxStackTraceLines: Int,
    levels: Map[String, Threshold] = Map.empty,
):

  def thresholdFor(loggerName: String): Threshold =
    if levels.isEmpty then level
    else
      levels.iterator
        .filter((pattern, _) => loggerName == pattern || loggerName.startsWith(s"$pattern."))
        .maxByOption((pattern, _) => pattern.length)
        .fold(level)(_._2)

object ConsoleConfig:

  private def parseThresholds(section: Config): Map[String, Threshold] =
    if !section.hasPath("levels") then Map.empty
    else
      val levels = section.getConfig("levels")
      levels
        .entrySet()
        .asScala
        .map(entry => ConfigUtil.splitPath(entry.getKey).asScala.mkString(".") -> Threshold.parse(entry.getValue.unwrapped().toString))
        .toMap

  private[console] def load(config: Config = ConfigFactory.load()): ConsoleConfig =
    val section = config.getConfig("logging4s.console")
    ConsoleConfig(
      level = Threshold.parse(section.getString("level")),
      format = Format.parse(section.getString("format")),
      color = ColorMode.parse(section.getString("color")),
      stream = Stream.parse(section.getString("stream")),
      maxStackTraceLines = section.getInt("max-stack-trace-lines"),
      levels = parseThresholds(section),
    )

  given default: ConsoleConfig = load()
