package logging4s.console

import org.scalatest.wordspec.AnyWordSpec
import org.scalatest.matchers.should.Matchers

import com.typesafe.config.ConfigFactory

import logging4s.core.Level

class ConsoleLevelResolutionSpec extends AnyWordSpec, Matchers:

  private def config(levels: (String, Level)*): ConsoleConfig =
    ConsoleConfig(Level.Info, Format.Json, ColorMode.Off, Stream.Stdout, -1, levels.toMap)

  "ConsoleConfig.levelFor" must:
    "fall back to the root level when there are no mappings" in:
      config().levelFor("com.acme.Service") shouldEqual Level.Info

    "fall back to the root level when nothing matches" in:
      config("io.netty" -> Level.Error).levelFor("com.acme.Service") shouldEqual Level.Info

    "use an exact match" in:
      config("com.acme.Service" -> Level.Debug).levelFor("com.acme.Service") shouldEqual Level.Debug

    "apply a prefix to child loggers" in:
      config("io.netty" -> Level.Error).levelFor("io.netty.channel.Pipeline") shouldEqual Level.Error

    "not treat a partial segment as a prefix" in:
      config("io.netty" -> Level.Error).levelFor("io.nettyfoo.Bar") shouldEqual Level.Info

    "prefer the most specific prefix" in:
      val cfg = config("io" -> Level.Error, "io.netty" -> Level.Warn, "io.netty.channel" -> Level.Debug)

      cfg.levelFor("io.netty.channel.Pipeline") shouldEqual Level.Debug
      cfg.levelFor("io.netty.buffer.Buf") shouldEqual Level.Warn
      cfg.levelFor("io.grpc.Server") shouldEqual Level.Error

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

      loaded.level shouldEqual Level.Info
      loaded.levels shouldEqual Map("io.netty" -> Level.Warn, "com.acme.Service" -> Level.Debug)

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
