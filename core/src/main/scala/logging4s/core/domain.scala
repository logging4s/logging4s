package logging4s.core

private[core] type Identity[A] = A

private[core] type ThrowableEither[A] = Either[Throwable, A]


 
