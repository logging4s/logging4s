package logging4s.cats

import cats.effect.{IO, IOLocal}

import logging4s.core.{LocalContext, LoggingContext}

object IOLocalContext:

  def make: IO[LocalContext[IO]] =
    IOLocal(LoggingContext.empty).map(from)

  def from(local: IOLocal[LoggingContext]): LocalContext[IO] =
    new:
      override def use[A](f: LoggingContext => IO[A]): IO[A] = local.get.flatMap(f)

      override def scoped[A](context: LoggingContext)(fa: IO[A]): IO[A] =
        local.modify(current => (current + context, current)).bracket(_ => fa)(local.set)
