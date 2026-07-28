package logging4s.log4j2

import scala.jdk.CollectionConverters.given

import org.apache.logging.log4j.message.StringMapMessage

final class LoggableMapMessage(entries: Map[String, String], plainMessage: String) extends StringMapMessage(entries.asJava):

  override def getFormattedMessage: String = plainMessage
