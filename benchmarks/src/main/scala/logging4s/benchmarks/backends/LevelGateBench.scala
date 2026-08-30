package logging4s.benchmarks.backends

import java.io.OutputStream
import java.util.concurrent.TimeUnit

import scala.util.Try

import org.openjdk.jmh.annotations.*
import org.openjdk.jmh.infra.Blackhole

import org.slf4j.LoggerFactory
import net.logstash.logback.encoder.LogstashEncoder
import ch.qos.logback.classic.spi.ILoggingEvent
import ch.qos.logback.core.OutputStreamAppender
import ch.qos.logback.classic.{Level as LogbackLevel, Logger as LogbackLogger}

import logging4s.core.{LoggableValue, Logging}
import logging4s.core.syntax.all.*

@State(Scope.Benchmark)
@BenchmarkMode(Array(Mode.Throughput))
@OutputTimeUnit(TimeUnit.SECONDS)
@Warmup(iterations = 3, time = 1)
@Measurement(iterations = 5, time = 1)
@Fork(1)
class LevelGateBench:

  import LevelGateBench.given

  private var atInfo: Logging[Try]  = scala.compiletime.uninitialized
  private var atTrace: Logging[Try] = scala.compiletime.uninitialized

  @Setup
  def setup(): Unit =
    atInfo = logger("bench-gate-info", LogbackLevel.INFO)
    atTrace = logger("bench-gate-trace", LogbackLevel.TRACE)

  private def logger(name: String, level: LogbackLevel): Logging[Try] =
    import logging4s.logback.LogbackInstances.given

    val underlying = LoggerFactory.getLogger(name).asInstanceOf[LogbackLogger]
    underlying.setLevel(level)
    underlying.setAdditive(false)

    val context = underlying.getLoggerContext
    val encoder = new LogstashEncoder()
    encoder.setContext(context)
    encoder.start()

    val appender = new OutputStreamAppender[ILoggingEvent]()
    appender.setContext(context)
    appender.setEncoder(encoder)
    appender.setOutputStream(OutputStream.nullOutputStream)
    appender.start()

    underlying.addAppender(appender)
    Logging.createTry(name).get

  private def eager: LoggableValue =
    val loggable = domain.jsoniter
    LoggableValue(loggable.key, loggable.plain(domain.sample), loggable.json(domain.sample))

  @Benchmark
  def disabledInterpolated(bh: Blackhole): Unit =
    given Logging[Try] = atInfo
    bh.consume(debug"event ${domain.sample}")

  @Benchmark
  def disabledDirectLazy(bh: Blackhole): Unit =
    bh.consume(atInfo.debug("event", domain.sample.asLogValue))

  @Benchmark
  def disabledDirectEager(bh: Blackhole): Unit =
    bh.consume(atInfo.debug("event", eager))

  @Benchmark
  def enabledInterpolated(bh: Blackhole): Unit =
    given Logging[Try] = atTrace
    bh.consume(debug"event ${domain.sample}")

  @Benchmark
  def enabledDirectLazy(bh: Blackhole): Unit =
    bh.consume(atTrace.debug("event", domain.sample.asLogValue))

object LevelGateBench:
  given logging4s.core.Loggable[domain.Event] = domain.jsoniter
