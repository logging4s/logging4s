package logging4s.examples

import logging4s.core.Logging

import java.util.UUID

object WithoutEffectsExample extends App:

  val unsafeLogging = Logging.createUnsafe("UnsafeExampleLogging")
  val tryLogging    = Logging.createTry("TryExampleLogging")
  val eitherLogging = Logging.createEither("EitherExampleLogging")

  val johnSnow     = User(UUID.randomUUID(), "John Show", 22)
  val daenerys     = User(UUID.randomUUID(), "Daenerys Targaryen", 22)
  val createdUsers = Seq(johnSnow, daenerys)

  unsafeLogging.info("User created", johnSnow)
  unsafeLogging.info("User created", daenerys)
  unsafeLogging.info("All users created", createdUsers)

  tryLogging.map(_.info("User created", johnSnow))
  tryLogging.map(_.info("User created", daenerys))
  tryLogging.map(_.info("All users created", createdUsers))

  eitherLogging.map(_.info("User created", johnSnow))
  eitherLogging.map(_.info("User created", daenerys))
  eitherLogging.map(_.info("All users created", createdUsers))
