package logging4s.examples

import java.util.UUID

import logging4s.core.Logging

import logging4s.logback.LogbackInstances.given

object WithoutEffectsExample extends App:

  private val unsafeLogging = Logging.createUnsafe("UnsafeExampleLogging")
  private val tryLogging    = Logging.createTry("TryExampleLogging")
  private val eitherLogging = Logging.createEither("EitherExampleLogging")

  private val johnSnow     = User(UUID.randomUUID(), "John Show", 22)
  private val daenerys     = User(UUID.randomUUID(), "Daenerys Targaryen", 22)
  private val createdUsers = Seq(johnSnow, daenerys)

  unsafeLogging.info("User created", johnSnow)
  unsafeLogging.info("User created", daenerys)
  unsafeLogging.info("All users created", createdUsers)

  tryLogging.map(_.info("User created", johnSnow))
  tryLogging.map(_.info("User created", daenerys))
  tryLogging.map(_.info("All users created", createdUsers))

  eitherLogging.map(_.info("User created", johnSnow))
  eitherLogging.map(_.info("User created", daenerys))
  eitherLogging.map(_.info("All users created", createdUsers))
