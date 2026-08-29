package logging4s.console

import scala.jdk.CollectionConverters.*

import com.typesafe.config.{Config, ConfigFactory, ConfigUtil}

import logging4s.core.Level

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
    levels: Map[String, Level] = Map.empty,
):

  def levelFor(loggerName: String): Level =
    if levels.isEmpty then level
    else
      levels.iterator
        .filter((pattern, _) => loggerName == pattern || loggerName.startsWith(s"$pattern."))
        .maxByOption((pattern, _) => pattern.length)
        .fold(level)(_._2)

object ConsoleConfig:

  private def parseLevel(raw: String): Level =
    Level.parse(raw).getOrElse(throw new IllegalArgumentException(s"Invalid logging4s.console.level: '$raw'"))

  private def parseLevels(section: Config): Map[String, Level] =
    if !section.hasPath("levels") then Map.empty
    else
      val levels = section.getConfig("levels")
      levels
        .entrySet()
        .asScala
        .map(entry => ConfigUtil.splitPath(entry.getKey).asScala.mkString(".") -> parseLevel(entry.getValue.unwrapped().toString))
        .toMap

  private[console] def load(config: Config = ConfigFactory.load()): ConsoleConfig =
    val section = config.getConfig("logging4s.console")
    ConsoleConfig(
      level = parseLevel(section.getString("level")),
      format = Format.parse(section.getString("format")),
      color = ColorMode.parse(section.getString("color")),
      stream = Stream.parse(section.getString("stream")),
      maxStackTraceLines = section.getInt("max-stack-trace-lines"),
      levels = parseLevels(section),
    )

  given default: ConsoleConfig = load()
