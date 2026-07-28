package logging4s.log4j2

import org.apache.logging.log4j.LogManager

// Forces Log4j2's lazy LoggerContext/Configuration initialization to complete exactly once (Scala object init is
// JVM-synchronized) before any spec in this module starts manipulating Configuration/LoggerConfig directly.
// Without this, the very first Log4j2 touch in the test JVM can race with LoggingLog4j2MessageSpec's dynamic
// reconfiguration: Log4j2's own first-time default-config setup can replace the Configuration object our test
// just added a LoggerConfig/appender to, silently dropping it.
private[log4j2] object Log4j2Warmup:
  LogManager.getLogger("logging4s-log4j2-warmup").info("warmup")

  def touch(): Unit = ()
