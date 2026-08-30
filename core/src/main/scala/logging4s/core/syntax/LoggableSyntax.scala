package logging4s.core.syntax

import logging4s.core.config.LoggableEncodingConfig
import logging4s.core.{JsonEncoder, Loggable, LoggableValue, PlainEncoder, ValueKey}

trait LoggableSyntax:

  extension [A](a: A)(using L: Loggable[A])
    def asLogValue: LoggableValue                    = LoggableValue.deferred(L.key, a, L)
    def asLogValue(key: String): LoggableValue       = LoggableValue.deferred(ValueKey(key), a, L)
    def mapPlain(f: String => String): LoggableValue = LoggableValue.deferred(L.key, a, L.mapPlain(f))

  extension [A](a: A)(using JE: JsonEncoder[A], PE: PlainEncoder[A])
    def withKey(key: String): LoggableValue = LoggableValue.deferred(ValueKey(key), a, Loggable.make[A](key)(JE.encode, PE.encode))

  extension (values: Seq[LoggableValue]) def plain(using cfg: LoggableEncodingConfig): String = cfg.plainValuesStyle.render(values)
