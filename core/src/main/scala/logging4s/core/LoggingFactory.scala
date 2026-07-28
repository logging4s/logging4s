package logging4s.core

trait LoggingFactory:
  def create[F[*]: Delay](name: String, context: LoggingContext): F[Logging[F]]
