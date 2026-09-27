package logging4s.core.conformance

import scala.util.Try

import org.scalatest.wordspec.AnyWordSpec
import org.scalatest.matchers.should.Matchers

import logging4s.core.{Level, Logging, LoggingContext}
import logging4s.core.syntax.all.*

final case class CapturedRecord(message: String, fields: Seq[(String, String)]):
  def keys: Seq[String]                  = fields.map(_._1)
  def apply(key: String): Option[String] = fields.collectFirst { case (k, v) if k == key => v }
  def count(key: String): Int            = keys.count(_ == key)

trait BackendConformance extends AnyWordSpec, Matchers:

  def backendName: String

  def capture(loggerName: String)(run: Logging[Try] => Unit): CapturedRecord

  def capturedAtLevel(loggerName: String, threshold: Level)(run: Logging[Try] => Unit): Option[CapturedRecord] = None

  s"$backendName, as a logging4s backend," must:

    "attach a call-site value as its own field" in:
      val record = capture(s"$backendName-conformance-value")(_.info("event", 7.asLogValue("count")))

      record("count") shouldEqual Some("7")

    "attach context values to every record" in:
      val record = capture(s"$backendName-conformance-context") { logging =>
        logging.withContext(LoggingContext("svc".asLogValue("service"))).info("event")
      }

      record("service") shouldEqual Some("\"svc\"")

    "let a call-site value override a context value with the same key" in:
      val record = capture(s"$backendName-conformance-override") { logging =>
        logging.withContext(LoggingContext(1.asLogValue("id"))).info("event", 2.asLogValue("id"))
      }

      record("id") shouldEqual Some("2")
      record.count("id") shouldEqual 1

    "let a later context replace an earlier one under the same key" in:
      val record = capture(s"$backendName-conformance-context-chain") { logging =>
        logging
          .withContext(LoggingContext(1.asLogValue("id")))
          .withContext(LoggingContext(2.asLogValue("id")))
          .info("event")
      }

      record("id") shouldEqual Some("2")
      record.count("id") shouldEqual 1

    "treat context keys that normalize to the same name as one key" in:
      val record = capture(s"$backendName-conformance-normalized") { logging =>
        logging
          .withContext(LoggingContext(1.asLogValue("userId")))
          .withContext(LoggingContext(2.asLogValue("user_id")))
          .info("event")
      }

      record.count("user_id") shouldEqual 1
      record("user_id") shouldEqual Some("2")

    "emit the call site as a source field, and never in the message text" in:
      val record = capture(s"$backendName-conformance-source")(_.info("event"))

      record("source") should not be empty
      record.message should not include "source"

    "never emit the same key twice, whatever the call site passes" in:
      val record = capture(s"$backendName-conformance-duplicates") { logging =>
        logging.info("event", 1.asLogValue("id"), 2.asLogValue("id"), 3.asLogValue("id_2"))
      }

      record.keys.distinct.size shouldEqual record.keys.size

    "keep a user value that collides with an envelope name out of the envelope" in:
      val record = capture(s"$backendName-conformance-reserved")(_.info("event", "forged".asLogValue("level")))

      record.count("level") should be <= 1

    "keep the static message text intact" in:
      val record = capture(s"$backendName-conformance-message")(_.info("user created", 7.asLogValue("id")))

      record.message should startWith("user created")
