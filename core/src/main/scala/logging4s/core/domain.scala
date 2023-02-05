package logging4s.core

type Identity[A] = A

type ThrowableEither[A] = Either[Throwable, A]

final case class LoggingContext(values: Seq[LoggableValue]):
  def +(other: LoggingContext): LoggingContext = LoggingContext(values ++ other.values)

object LoggingContext:
  def apply(value: LoggableValue): LoggingContext = LoggingContext(Seq(value))
