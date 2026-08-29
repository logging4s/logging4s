package logging4s.console

import java.io.{ByteArrayOutputStream, PrintStream}

import scala.util.Try

import org.scalatest.matchers.should.Matchers
import org.scalatest.wordspec.AnyWordSpec

import logging4s.core.{JsonString, Level, Logging, LoggableValue, PlainString, ValueKey}
import logging4s.core.config.LoggableEncodingConfig

import ConsoleInstances.given

class LoggingConsoleSpec extends AnyWordSpec, Matchers:

  private def capture(
      config: ConsoleConfig,
      encoding: LoggableEncodingConfig = LoggableEncodingConfig.Default,
      name: String = "ConsoleSpec",
  )(run: Logging[Try] => Unit): String =
    given ConsoleConfig          = config
    given LoggableEncodingConfig = encoding

    val out      = new ByteArrayOutputStream()
    val original = System.out
    System.setOut(new PrintStream(out, true, "UTF-8"))
    try
      val logging = Logging.createTry(name).get
      run(logging)
    finally System.setOut(original)

    out.toString("UTF-8")

  private val jsonAtInfo = ConsoleConfig(Level.Info, Format.Json, ColorMode.Off, Stream.Stdout, -1)

  "LoggingConsole" must:
    "emit structured values as nested JSON with an envelope" in:
      val out = capture(jsonAtInfo) { logging =>
        logging.info("user created", LoggableValue(ValueKey("user"), PlainString("id=1"), JsonString("""{"id":1,"name":"John"}""")))
      }

      out should include(""""user":{"id":1,"name":"John"}""")
      out should include(""""level":"INFO"""")
      out should include(""""logger":"ConsoleSpec"""")
      out should include(""""message":"user created""")

    "not emit anything below the configured level" in:
      val out = capture(jsonAtInfo) { logging =>
        logging.debug("noisy", LoggableValue(ValueKey("k"), PlainString("v"), JsonString("1")))
      }

      out.trim shouldBe empty

    "render the plain format when configured" in:
      val out = capture(ConsoleConfig(Level.Info, Format.Plain, ColorMode.Off, Stream.Stdout, -1)) { logging =>
        logging.info("hello", LoggableValue(ValueKey("k"), PlainString("v"), JsonString("\"v\"")))
      }

      out should include("INFO ConsoleSpec - hello")
      out should include("k -> (v)")

    "attach context values to a message logged without call-site values" in:
      val out = capture(jsonAtInfo) { logging =>
        logging
          .withContextValues(LoggableValue(ValueKey("session"), PlainString("abc"), JsonString("\"abc\"")))
          .info("no call-site values")
      }

      out should include(""""session":"abc"""")

    "omit the source field when the config disables it" in:
      val out = capture(jsonAtInfo, LoggableEncodingConfig(includeSourcePosition = false)) { logging =>
        logging.info("no source here")
      }

      out should not include "\"source\""
      out should include(""""message":"no source here"""")

    "include the source field by default" in:
      val out = capture(jsonAtInfo) { logging =>
        logging.info("with source")
      }

      out should include(""""source":"LoggingConsoleSpec.scala:""")

    "suppress a logger whose own mapped level is higher than the record" in:
      val config = jsonAtInfo.copy(levels = Map("io.netty" -> Level.Error))

      val quiet = capture(config, name = "io.netty.channel.Pipeline") { logging =>
        logging.info("noisy")
      }

      quiet.trim shouldBe empty

    "keep loggers that no mapping matches at the root level" in:
      val config = jsonAtInfo.copy(levels = Map("io.netty" -> Level.Error))

      val loud = capture(config, name = "com.acme.Service") { logging =>
        logging.info("kept")
      }

      loud should include("kept")
