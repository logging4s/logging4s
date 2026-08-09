package logging4s.log4j2

import org.apache.logging.log4j.LogManager

private[log4j2] object Log4j2Warmup:
  LogManager.getLogger("logging4s-log4j2-warmup").info("warmup")

  def touch(): Unit = ()
