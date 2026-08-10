package logging4s.benchmarks

import java.util.concurrent.TimeUnit

import org.openjdk.jmh.annotations.*
import org.openjdk.jmh.infra.Blackhole

import com.github.plokhotnyuk.jsoniter_scala.core.JsonValueCodec
import com.github.plokhotnyuk.jsoniter_scala.macros.JsonCodecMaker

import logging4s.core.{Loggable, PlainEncoder, PlainString}
import logging4s.json.jsoniter.JsoniterInstances.given

final case class Bench(id: Int, name: String, age: Int, active: Boolean, score: Double, tag: String)

object JsonDerivationBench:
  val value: Bench = Bench(42, "John Doe", 33, true, 9.75, "premium")

  given JsonValueCodec[Bench] = JsonCodecMaker.make
  given PlainEncoder[Bench]   = (b: Bench) => PlainString(b.toString)

  val derivedLoggable: Loggable[Bench]      = Loggable.derived
  val makeLoggable: Loggable[Bench]         = Loggable.fromEncoders[Bench]("bench")
  val fromEncodersLoggable: Loggable[Bench] = Loggable.fromEncoders

@State(Scope.Benchmark)
@BenchmarkMode(Array(Mode.AverageTime))
@OutputTimeUnit(TimeUnit.NANOSECONDS)
@Warmup(iterations = 3, time = 1)
@Measurement(iterations = 5, time = 1)
@Fork(1)
class JsonDerivationBench:

  import JsonDerivationBench.*

  @Benchmark
  def derived(bh: Blackhole): Unit =
    bh.consume(derivedLoggable.json(value).value)

  @Benchmark
  def make(bh: Blackhole): Unit =
    bh.consume(makeLoggable.json(value).value)

  @Benchmark
  def fromEncoders(bh: Blackhole): Unit =
    bh.consume(fromEncodersLoggable.json(value).value)
