package logging4s.log4j2

import org.apache.logging.log4j.message.StringMapMessage

import scala.jdk.CollectionConverters.given

import logging4s.core.JsonString

final class LoggableMapMessage(
    entries: Seq[(String, String)],
    plainMessage: String,
) extends StringMapMessage(entries.toMap.asJava):

  override def getFormattedMessage: String = plainMessage

  override def getFormattedMessage(formats: Array[String]): String =
    if formats != null && formats.exists(_.equalsIgnoreCase("JSON"))
    then entries.iterator.map((key, value) => s"${JsonString.quoted(key).value}:$value").mkString("{", ",", "}")
    else super.getFormattedMessage(formats)
