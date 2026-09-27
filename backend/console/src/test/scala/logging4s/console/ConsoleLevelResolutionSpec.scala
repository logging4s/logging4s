package logging4s.console

import org.scalatest.wordspec.AnyWordSpec
import org.scalatest.matchers.should.Matchers

import com.typesafe.config.ConfigFactory

import logging4s.core.Level

class ConsoleLevelResolutionSpec extends AnyWordSpec, Matchers:

  private def config(levels: (String, Threshold)*): ConsoleConfig =
    ConsoleConfig(Threshold.At(Level.Info), Format.Json, ColorMode.Off, Stream.Stdout, -1, levels.toMap)

  "ConsoleConfig.thresholdFor" must:
    "fall back to the root level when there are no mappings" in:
      config().thresholdFor("com.acme.Service") shouldEqual Threshold.At(Level.Info)

    "fall back to the root level when nothing matches" in:
      config("io.netty" -> Threshold.At(Level.Error)).thresholdFor("com.acme.Service") shouldEqual Threshold.At(Level.Info)

    "use an exact match" in:
      config("com.acme.Service" -> Threshold.At(Level.Debug)).thresholdFor("com.acme.Service") shouldEqual Threshold.At(Level.Debug)

    "apply a prefix to child loggers" in:
      config("io.netty" -> Threshold.At(Level.Error)).thresholdFor("io.netty.channel.Pipeline") shouldEqual Threshold.At(Level.Error)

    "not treat a partial segment as a prefix" in:
      config("io.netty" -> Threshold.At(Level.Error)).thresholdFor("io.nettyfoo.Bar") shouldEqual Threshold.At(Level.Info)

    "prefer the most specific prefix" in:
      val cfg = config("io" -> Threshold.At(Level.Error), "io.netty" -> Threshold.At(Level.Warn), "io.netty.channel" -> Threshold.At(Level.Debug))

      cfg.thresholdFor("io.netty.channel.Pipeline") shouldEqual Threshold.At(Level.Debug)
      cfg.thresholdFor("io.netty.buffer.Buf") shouldEqual Threshold.At(Level.Warn)
      cfg.thresholdFor("io.grpc.Server") shouldEqual Threshold.At(Level.Error)

  "ConsoleConfig loading" must:
    "read quoted and nested mapping keys the same way" in:
      val raw = ConfigFactory.parseString(
        """logging4s.console {
          |  level = "info"
          |  format = "json"
          |  color = "off"
          |  stream = "stdout"
          |  max-stack-trace-lines = -1
          |  levels {
          |    "io.netty" = "warn"
          |    com.acme.Service = "debug"
          |  }
          |}""".stripMargin
      )

      val loaded = ConsoleConfig.load(raw)

      loaded.level shouldEqual Threshold.At(Level.Info)
      loaded.levels shouldEqual Map("io.netty" -> Threshold.At(Level.Warn), "com.acme.Service" -> Threshold.At(Level.Debug))

    "default to no mappings when the section is absent" in:
      val raw = ConfigFactory.parseString(
        """logging4s.console {
          |  level = "warn"
          |  format = "plain"
          |  color = "off"
          |  stream = "stderr"
          |  max-stack-trace-lines = 5
          |}""".stripMargin
      )

      ConsoleConfig.load(raw).levels shouldBe empty

    "read off as a threshold that lets nothing through" in:
      val raw = ConfigFactory.parseString(
        """logging4s.console {
          |  level = "info"
          |  format = "json"
          |  color = "off"
          |  stream = "stdout"
          |  max-stack-trace-lines = -1
          |  levels {
          |    "io.netty" = "off"
          |  }
          |}""".stripMargin
      )

      val loaded = ConsoleConfig.load(raw)

      loaded.thresholdFor("io.netty.channel.Pipeline") shouldEqual Threshold.Off
      Level.values.foreach(level => loaded.thresholdFor("io.netty").allows(level) shouldEqual false)
      loaded.thresholdFor("com.acme.Service").allows(Level.Info) shouldEqual true

    "turn the whole console off when the root level is off" in:
      val raw = ConfigFactory.parseString(
        """logging4s.console {
          |  level = "off"
          |  format = "json"
          |  color = "off"
          |  stream = "stdout"
          |  max-stack-trace-lines = -1
          |}""".stripMargin
      )

      val loaded = ConsoleConfig.load(raw)

      Level.values.foreach(level => loaded.thresholdFor("anything").allows(level) shouldEqual false)
