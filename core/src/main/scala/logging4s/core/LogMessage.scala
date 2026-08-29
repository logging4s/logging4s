package logging4s.core

import logging4s.core.config.LoggableEncodingConfig
import logging4s.core.syntax.all.plain

object LogMessage:

  def render(message: String, cause: Option[Throwable], values: Seq[LoggableValue])(using LoggableEncodingConfig): String =
    cause match
      case None        => if values.isEmpty then message else s"$message: ${values.plain}"
      case Some(error) =>
        val base = s"$message: class=${error.getClass.getName}, message=${error.getMessage}"
        if values.isEmpty then base else s"$base, ${values.plain}"
