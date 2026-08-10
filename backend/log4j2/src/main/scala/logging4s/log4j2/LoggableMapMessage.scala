package logging4s.log4j2

import org.apache.logging.log4j.message.StringMapMessage

import scala.jdk.CollectionConverters.given

final class LoggableMapMessage(entries: Map[String, String], plainMessage: String) extends StringMapMessage(entries.asJava):

  override def getFormattedMessage: String = plainMessage

  override def getFormattedMessage(formats: Array[String]): String =
    if formats != null && formats.exists(_.equalsIgnoreCase("JSON"))
    then entries.iterator.map((key, value) => s"\"$key\":$value").mkString("{", ",", "}")
    else super.getFormattedMessage(formats)
