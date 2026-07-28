package logging4s.logback

import org.slf4j.LoggerFactory

// Forces logback's lazy default-configuration initialization to complete exactly once (Scala object init is
// JVM-synchronized) before any spec in this module starts manipulating a Logger's appenders directly. Without this,
// the very first logback touch in the shared test JVM (sbt runs every project's tests in one JVM by default) can
// race with LoggingLogbackJsonSpec's dynamic appender attachment, occasionally causing the freshly attached
// ListAppender to miss the event it was meant to capture.
private[logback] object LogbackWarmup:
  LoggerFactory.getLogger("logging4s-logback-warmup").info("warmup")

  def touch(): Unit = ()
