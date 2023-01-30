package logging4s.core

import org.scalatest.matchers.should.Matchers
import org.scalatest.wordspec.AnyWordSpec

import scala.util.Try

class LoggingSpec extends AnyWordSpec with Matchers:

  "Logging" must {

    "right create for default Delay implementations" in {
      val resultTry = for
        logging <- Logging.create[Try]("LoggingSpec")
        _       <- logging.info("Test log")
      yield ()

      assert(resultTry.isSuccess)

      val resultEither =
        for
          logging <- Logging.createEither("LoggingSpec")
          _       <- logging.info("Test log")
        yield ()

      assert(resultEither.isRight)

      Logging
        .createUnsafe("LoggingSpec")
        .info("Test log")
    }

  }
