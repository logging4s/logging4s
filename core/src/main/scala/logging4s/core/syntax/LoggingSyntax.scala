package logging4s.core.syntax

import logging4s.core.Logging
import logging4s.core.interpolation.LoggingInterpolator

trait LoggingSyntax:

  extension (inline sc: StringContext)
    inline def info[F[*]](inline args: Any*)(using logging: Logging[F]): F[Unit] =
      ${ LoggingInterpolator.infoImpl[F]('sc, 'args, 'logging) }

    inline def warn[F[*]](inline args: Any*)(using logging: Logging[F]): F[Unit] =
      ${ LoggingInterpolator.warnImpl[F]('sc, 'args, 'logging) }

    inline def error[F[*]](inline args: Any*)(using logging: Logging[F]): F[Unit] =
      ${ LoggingInterpolator.errorImpl[F]('sc, 'args, 'logging) }

    inline def debug[F[*]](inline args: Any*)(using logging: Logging[F]): F[Unit] =
      ${ LoggingInterpolator.debugImpl[F]('sc, 'args, 'logging) }

    inline def trace[F[*]](inline args: Any*)(using logging: Logging[F]): F[Unit] =
      ${ LoggingInterpolator.traceImpl[F]('sc, 'args, 'logging) }
