package logging4s.examples

import cats.effect.{ExitCode, IO, IOApp}
import logging4s.cats.LoggingCats

object CatsEffect3Example extends IOApp:

  private def createUser(name: String, age: Int): IO[User] =
    for id <- IO.randomUUID
    yield User(id, name, age)

  override def run(args: List[String]): IO[ExitCode] =
    for
      logging <- LoggingCats.create[IO]("CatsEffectExampleLogging")

      johnShow <- createUser("John Show", 22)
      _        <- logging.info("User created", johnShow)

      daenerys <- createUser("Daenerys Targaryen", 22)
      _        <- logging.info("User created", daenerys)

      _ <- logging.info("All users created", Seq(johnShow, daenerys))
    yield ExitCode.Success
