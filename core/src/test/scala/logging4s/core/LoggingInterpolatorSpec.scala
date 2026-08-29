package logging4s.core

import org.scalatest.wordspec.AnyWordSpec
import org.scalatest.matchers.should.Matchers

import logging4s.core.syntax.all.*

class LoggingInterpolatorSpec extends AnyWordSpec, Matchers:

  final class Capturing extends Logging[Identity]:
    var level: Level               = Level.Info
    var enabledLevels: Set[Level]  = Level.values.toSet
    var message: String            = ""
    var values: Seq[LoggableValue] = Seq.empty

    def withContext(context: LoggingContext): Logging[Identity] = this

    def enabled(level: Level): Boolean = enabledLevels.contains(level)

    def unit: Unit = ()

    def emit(level: Level, message: String, cause: Option[Throwable], values: Seq[LoggableValue]): Unit =
      this.level = level
      this.message = message
      this.values = values

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

    "evaluate each interpolated expression exactly once" in:
      val log                 = new Capturing
      given Logging[Identity] = log

      var evaluations    = 0
      def counted(): Int =
        evaluations += 1
        evaluations

      info"value ${counted()}"

      evaluations shouldEqual 1
      log.values.head.plain shouldEqual "1"

    "skip emit entirely when the level is disabled" in:
      val log                 = new Capturing
      given Logging[Identity] = log

      log.enabledLevels = Set.empty

      var evaluations    = 0
      def counted(): Int =
        evaluations += 1
        evaluations

      debug"value ${counted()}"

      log.message shouldEqual ""
      log.values shouldEqual Seq.empty
      evaluations shouldEqual 0

    "still emit when the level is enabled" in:
      val log                 = new Capturing
      given Logging[Identity] = log

      log.enabledLevels = Set(Level.Debug)

      val count = 5
      debug"value $count"

      log.level shouldEqual Level.Debug
      log.message shouldEqual "value"

    "route the level to the matching Logging method" in:
      val log                 = new Capturing
      given Logging[Identity] = log

      val count = 5
      warn"warned $count"

      log.level shouldEqual Level.Warn
      log.message shouldEqual "warned"
      log.values.map(_.key) shouldEqual Seq(ValueKey("count"))
