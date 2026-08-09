package logging4s.logback

import org.slf4j.LoggerFactory

private[logback] object LogbackWarmup:
  LoggerFactory.getLogger("logging4s-logback-warmup").info("warmup")

  def touch(): Unit = ()
