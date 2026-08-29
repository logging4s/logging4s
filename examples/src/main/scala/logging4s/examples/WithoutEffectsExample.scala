package logging4s.examples

import java.util.UUID

import scala.language.implicitConversions
import scala.util.Try

import logging4s.core.{Logging, ThrowableEither}

import logging4s.logback.LogbackInstances.given

object WithoutEffectsExample extends App:

  private val unsafeLogging = Logging.createUnsafe("UnsafeExampleLogging")

  private val johnSnow     = User(UUID.randomUUID(), "John Show", 22)
  private val daenerys     = User(UUID.randomUUID(), "Daenerys Targaryen", 22)
  private val createdUsers = Seq(johnSnow, daenerys)

  unsafeLogging.info("User created", johnSnow)
  unsafeLogging.info("User created", daenerys)
  unsafeLogging.info("All users created", createdUsers)

  private val tryResult: Try[Unit] =
    for
      logging <- Logging.createTry("TryExampleLogging")
      _       <- logging.info("User created", johnSnow)
      _       <- logging.info("User created", daenerys)
      _       <- logging.info("All users created", createdUsers)
    yield ()

  private val eitherResult: ThrowableEither[Unit] =
    for
      logging <- Logging.createEither("EitherExampleLogging")
      _       <- logging.info("User created", johnSnow)
      _       <- logging.info("User created", daenerys)
      _       <- logging.info("All users created", createdUsers)
    yield ()

  println(s"try logging succeeded: ${tryResult.isSuccess}")
  println(s"either logging succeeded: ${eitherResult.isRight}")
