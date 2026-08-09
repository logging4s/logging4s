package logging4s.core

import org.scalatest.wordspec.AnyWordSpec
import org.scalatest.matchers.should.Matchers

import logging4s.core.syntax.all.*

class LoggingInterpolatorSpec extends AnyWordSpec, Matchers:

  final class Capturing extends Logging[Identity]:
    var message: String            = ""
    var values: Seq[LoggableValue] = Seq.empty

    private def capture(m: String, vs: Seq[LoggableValue]): Unit =
      message = m
      values = vs

    def withContext(context: LoggingContext): Logging[Identity] = this

    def error(message: String): Unit                                           = ()
    def error(message: String, error: Throwable): Unit                         = ()
    def error(message: String, values: LoggableValue*): Unit                   = capture(message, values)
    def error(message: String, error: Throwable, values: LoggableValue*): Unit = ()

    def warn(message: String): Unit                                           = ()
    def warn(message: String, error: Throwable): Unit                         = ()
    def warn(message: String, values: LoggableValue*): Unit                   = capture(message, values)
    def warn(message: String, error: Throwable, values: LoggableValue*): Unit = ()

    def info(message: String): Unit                                           = ()
    def info(message: String, error: Throwable): Unit                         = ()
    def info(message: String, values: LoggableValue*): Unit                   = capture(message, values)
    def info(message: String, error: Throwable, values: LoggableValue*): Unit = ()

    def debug(message: String): Unit                                           = ()
    def debug(message: String, error: Throwable): Unit                         = ()
    def debug(message: String, values: LoggableValue*): Unit                   = capture(message, values)
    def debug(message: String, error: Throwable, values: LoggableValue*): Unit = ()

    def trace(message: String): Unit                                           = ()
    def trace(message: String, error: Throwable): Unit                         = ()
    def trace(message: String, values: LoggableValue*): Unit                   = capture(message, values)
    def trace(message: String, error: Throwable, values: LoggableValue*): Unit = ()

  "The log interpolator" must:
    "lower to Logging.info with the literal message and the value keyed by identifier name" in:
      val log                 = new Capturing
      given Logging[Identity] = log

      val p = Point(9, 9)
      info"pt $p"

      log.message shouldEqual "pt"
      log.values.map(_.key) shouldEqual Seq(ValueKey("p"))
      log.values.head.json shouldEqual """{"x":9,"y":9}"""

    "use the identifier name as key for scalars, not the Loggable's own key" in:
      val log                 = new Capturing
      given Logging[Identity] = log

      val count = 5
      info"count $count"

      log.message shouldEqual "count"
      log.values.map(_.key) shouldEqual Seq(ValueKey("count"))
      log.values.head.plain shouldEqual "5"

    "strip a trailing separator so the backend adds its own" in:
      val log                 = new Capturing
      given Logging[Identity] = log

      val count = 5
      info"saved: $count"

      log.message shouldEqual "saved"

    "attach several interpolated values in order" in:
      val log                 = new Capturing
      given Logging[Identity] = log

      val user = Point(1, 2)
      val n    = 3
      info"created $user with $n retries"

      log.values.map(_.key) shouldEqual Seq(ValueKey("user"), ValueKey("n"))

    "use an explicit LoggableValue as-is, keeping its key" in:
      val log                 = new Capturing
      given Logging[Identity] = log

      val count = 5
      info"n ${count.asLogValue("retries")}"

      log.values.map(_.key) shouldEqual Seq(ValueKey("retries"))

    "route the level to the matching Logging method" in:
      val log                 = new Capturing
      given Logging[Identity] = log

      val count = 5
      warn"warned $count"

      log.message shouldEqual "warned"
      log.values.map(_.key) shouldEqual Seq(ValueKey("count"))
