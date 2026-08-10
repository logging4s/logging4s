package logging4s.console

import java.io.{ByteArrayOutputStream, PrintStream}

import scala.util.Try

import org.scalatest.matchers.should.Matchers
import org.scalatest.wordspec.AnyWordSpec

import logging4s.core.{JsonString, Logging, LoggableValue, PlainString, ValueKey}

import ConsoleInstances.given

class LoggingConsoleSpec extends AnyWordSpec, Matchers:

  private def capture(config: ConsoleConfig)(run: Logging[Try] => Unit): String =
    given ConsoleConfig = config

    val out      = new ByteArrayOutputStream()
    val original = System.out
    System.setOut(new PrintStream(out, true, "UTF-8"))
    try
      val logging = Logging.createTry("ConsoleSpec").get
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
