package logging4s.benchmarks.backends

import java.io.{OutputStream, PrintStream}
import java.util.concurrent.TimeUnit

import scala.util.Try

import org.openjdk.jmh.annotations.*
import org.openjdk.jmh.infra.Blackhole

import org.slf4j.LoggerFactory
import ch.qos.logback.classic.{Level as LogbackLevel, Logger as LogbackLogger}
import ch.qos.logback.classic.spi.ILoggingEvent
import ch.qos.logback.core.OutputStreamAppender
import ch.qos.logback.core.encoder.Encoder as LogbackEncoder
import net.logstash.logback.encoder.LogstashEncoder

import org.apache.logging.log4j.{Level as Log4jLevel, LogManager}
import org.apache.logging.log4j.core.{Layout, LogEvent, LoggerContext}
import org.apache.logging.log4j.core.appender.AbstractAppender
import org.apache.logging.log4j.core.config.{AppenderRef, LoggerConfig, Property}
import org.apache.logging.log4j.layout.template.json.JsonTemplateLayout

import logging4s.core.{Loggable, LoggableValue, Logging}
import logging4s.logback.Logging4sEncoder

@State(Scope.Benchmark)
@BenchmarkMode(Array(Mode.Throughput))
@OutputTimeUnit(TimeUnit.SECONDS)
@Warmup(iterations = 3, time = 1)
@Measurement(iterations = 5, time = 1)
@Fork(1)
class BackendsBench:

  private val timestampAndLevel =
    """"@timestamp":{"$resolver":"timestamp"},"level":{"$resolver":"level","field":"name"}"""

  private val defaultTemplate = s"""{$timestampAndLevel,"message":{"$$resolver":"message","stringified":true}}"""
  private val nestedTemplate  = s"""{$timestampAndLevel,"message":{"$$resolver":"message"}}"""

  private var consoleLog: Logging[Try]     = scala.compiletime.uninitialized
  private var logbackDefault: Logging[Try] = scala.compiletime.uninitialized
  private var logbackOurs: Logging[Try]    = scala.compiletime.uninitialized
  private var log4j2Default: Logging[Try]  = scala.compiletime.uninitialized
  private var log4j2Ours: Logging[Try]     = scala.compiletime.uninitialized

  @Setup
  def setup(): Unit =
    System.setOut(new PrintStream(OutputStream.nullOutputStream))

    consoleLog = console()
    logbackDefault = logback("bench-logback-default", new LogstashEncoder())
    logbackOurs = logback("bench-logback-ours", new Logging4sEncoder())
    log4j2Default = log4j2("bench-log4j2-default", defaultTemplate)
    log4j2Ours = log4j2("bench-log4j2-ours", nestedTemplate)

  private def console(): Logging[Try] =
    import logging4s.console.ConsoleInstances.given
    import logging4s.console.{ColorMode, ConsoleConfig, Format, Level as ConsoleLevel, Stream}
    given ConsoleConfig = ConsoleConfig(ConsoleLevel.Trace, Format.Json, ColorMode.Off, Stream.Stdout, -1)
    Logging.createTry("bench-console").get

  private def logback(name: String, encoder: LogbackEncoder[ILoggingEvent]): Logging[Try] =
    import logging4s.logback.LogbackInstances.given

    val logger = LoggerFactory.getLogger(name).asInstanceOf[LogbackLogger]
    logger.setLevel(LogbackLevel.TRACE)
    logger.setAdditive(false)

    val context = logger.getLoggerContext
    encoder.setContext(context)
    encoder.start()

    val appender = new OutputStreamAppender[ILoggingEvent]()
    appender.setContext(context)
    appender.setEncoder(encoder)
    appender.setOutputStream(OutputStream.nullOutputStream)
    appender.start()

    logger.addAppender(appender)
    Logging.createTry(name).get

  private def log4j2(name: String, template: String): Logging[Try] =
    import logging4s.log4j2.Log4j2Instances.given

    val context = LogManager.getContext(false).asInstanceOf[LoggerContext]
    val config  = context.getConfiguration
    val layout  = JsonTemplateLayout.newBuilder().setConfiguration(config).setEventTemplate(template).build()

    val appender = new NullLayoutAppender(s"$name-appender", layout)
    appender.start()
    config.addAppender(appender)

    val ref          = AppenderRef.createAppenderRef(s"$name-appender", null, null)
    val loggerConfig = LoggerConfig.createLogger(false, Log4jLevel.TRACE, name, "true", Array(ref), null, config, null)
    loggerConfig.addAppender(appender, null, null)
    config.addLogger(name, loggerConfig)
    context.updateLoggers()

    Logging.createTry(name).get

  private def value(loggable: Loggable[domain.Event]): LoggableValue =
    LoggableValue(loggable.key, loggable.plain(domain.sample), loggable.json(domain.sample))

  @Benchmark def consoleDerive(bh: Blackhole): Unit     = bh.consume(consoleLog.info("event", value(domain.derived)))
  @Benchmark def consoleJsoniter(bh: Blackhole): Unit   = bh.consume(consoleLog.info("event", value(domain.jsoniter)))
  @Benchmark def logbackStandard(bh: Blackhole): Unit   = bh.consume(logbackDefault.info("event", value(domain.jsoniter)))
  @Benchmark def logbackOurEncoder(bh: Blackhole): Unit = bh.consume(logbackOurs.info("event", value(domain.jsoniter)))
  @Benchmark def log4j2Standard(bh: Blackhole): Unit    = bh.consume(log4j2Default.info("event", value(domain.jsoniter)))
  @Benchmark def log4j2OurLayout(bh: Blackhole): Unit   = bh.consume(log4j2Ours.info("event", value(domain.jsoniter)))

class NullLayoutAppender(name: String, layout: Layout[String])
    extends AbstractAppender(name, null, layout, true, Property.EMPTY_ARRAY):

  override def append(event: LogEvent): Unit =
    val _ = getLayout.toByteArray(event)
