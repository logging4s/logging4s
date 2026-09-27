package logging4s.core

import org.scalatest.wordspec.AnyWordSpec
import org.scalatest.matchers.should.Matchers

class StaticMessageSpec extends AnyWordSpec, Matchers:

  private val log: Logging[Identity] = Logging.noop[Identity]

  "A log message" must:
    "accept a plain literal" in:
      assertCompiles("""log.info("user created")""")

    "accept an interpolation without holes, which is still static text" in:
      assertCompiles("""log.info(s"user created")""")

    "accept a message computed elsewhere, which may legitimately vary" in:
      assertCompiles("""
        val chosen = if true then "user created" else "user updated"
        log.info(chosen)
      """)

    "reject an interpolated message, so values are passed as values" in:
      assertDoesNotCompile("""
        val id = 1
        log.info(s"user $id created")
      """)

    "reject an interpolated message on every level and overload" in:
      assertDoesNotCompile("""val id = 1; log.error(s"failed for $id")""")
      assertDoesNotCompile("""val id = 1; log.warn(s"odd $id", new RuntimeException)""")
      assertDoesNotCompile("""val id = 1; log.debug(s"seen $id", 5.asLogValue("n"))""")
      assertDoesNotCompile("""val id = 1; log.trace(s"seen $id", new RuntimeException, 5.asLogValue("n"))""")

  "Logging.noop" must:
    "report every level as disabled and do nothing" in:
      Level.values.foreach(level => log.enabled(level) shouldEqual false)
      log.info("ignored") shouldEqual ()
