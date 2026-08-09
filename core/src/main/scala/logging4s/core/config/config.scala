package logging4s.core.config

import java.util.regex.Pattern

import logging4s.core.{LoggableValue, PlainString}

enum KeyNameStyle:
  case AsIs, SnakeCase, KebabCase, CamelCase, PascalCase

  def format(name: String): String =
    this match
      case AsIs       => name
      case SnakeCase  => words(name).map(_.toLowerCase).mkString("_")
      case KebabCase  => words(name).map(_.toLowerCase).mkString("-")
      case PascalCase => words(name).map(capitalize).mkString
      case CamelCase  =>
        words(name) match
          case Nil          => ""
          case head :: tail => (head.toLowerCase +: tail.map(capitalize)).mkString

private val AcronymBoundary = Pattern.compile("([A-Z]+)([A-Z][a-z])")
private val CamelBoundary   = Pattern.compile("([a-z0-9])([A-Z])")
private val WordSeparator   = Pattern.compile("[_\\s-]+")

private[config] def words(name: String): List[String] =
  val withAcronymBoundary = AcronymBoundary.matcher(name).replaceAll("$1_$2")
  val withCamelBoundary   = CamelBoundary.matcher(withAcronymBoundary).replaceAll("$1_$2")
  WordSeparator
    .split(withCamelBoundary)
    .iterator
    .filter(_.nonEmpty)
    .toList

private[config] def capitalize(word: String): String =
  if word.isEmpty then word else s"${word.head.toUpper}${word.tail.toLowerCase}"

enum PlainTupleStyle:
  case AsScala, AsArray, Bare, Braces

  def render(elements: Seq[PlainString]): PlainString =
    this match
      case AsScala => PlainString(elements.mkString("(", ", ", ")"))
      case AsArray => PlainString(elements.mkString("[", ", ", "]"))
      case Bare    => PlainString(elements.mkString(", "))
      case Braces  => PlainString(elements.mkString("{", ", ", "}"))

enum PlainValuesStyle:
  case Arrow, ArrowBare, Logfmt, KeyValueComma, Colon, Bracketed, Parens, CurlyMap

  def render(values: Seq[LoggableValue]): String =
    this match
      case Arrow         => values.map(v => s"${v.key} -> (${v.plain})").mkString(", ")
      case ArrowBare     => values.map(v => s"${v.key} -> ${v.plain}").mkString(", ")
      case Logfmt        => values.map(v => s"${v.key}=${v.plain}").mkString(" ")
      case KeyValueComma => values.map(v => s"${v.key}=${v.plain}").mkString(", ")
      case Colon         => values.map(v => s"${v.key}: ${v.plain}").mkString(", ")
      case Bracketed     => values.map(v => s"[${v.key}=${v.plain}]").mkString(" ")
      case Parens        => values.map(v => s"${v.key}(${v.plain})").mkString(" ")
      case CurlyMap      => values.map(v => s"${v.key}=${v.plain}").mkString("{", ", ", "}")

final case class LoggableEncodingConfig(
    jsonTupleAsArray: Boolean = true,
    keyNameStyle: KeyNameStyle = KeyNameStyle.SnakeCase,
    plainTupleStyle: PlainTupleStyle = PlainTupleStyle.AsScala,
    plainValuesStyle: PlainValuesStyle = PlainValuesStyle.Arrow,
)

object LoggableEncodingConfig:
  val Default: LoggableEncodingConfig = LoggableEncodingConfig()

  given default: LoggableEncodingConfig = Default
