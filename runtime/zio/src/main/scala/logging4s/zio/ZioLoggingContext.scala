package logging4s.zio

import scala.annotation.targetName

import zio.{FiberRef, Task, Trace, UIO, Unsafe, ZIO}

import logging4s.core.{LocalContext, LoggableValue, LoggingContext}

object ZioLoggingContext:

  private val ref: FiberRef[LoggingContext] =
    Unsafe.unsafe(unsafe ?=> FiberRef.unsafe.make(LoggingContext.empty, identity, (parent, _) => parent))

  def get(using Trace): UIO[LoggingContext] = ref.get

  def use[R, E, A](f: LoggingContext => ZIO[R, E, A])(using Trace): ZIO[R, E, A] = ref.getWith(f)

  def scoped[R, E, A](context: LoggingContext)(zio: ZIO[R, E, A])(using Trace): ZIO[R, E, A] =
    ref.locallyWith(_ + context)(zio)

  @targetName("scopedValues")
  def scoped[R, E, A](values: LoggableValue*)(zio: ZIO[R, E, A])(using Trace): ZIO[R, E, A] =
    scoped(LoggingContext(values))(zio)

trait LocalContextInstance:

  given ZioTaskLocalContext: LocalContext[Task] = new:
    override def use[A](f: LoggingContext => Task[A]): Task[A]            = ZioLoggingContext.use(f)
    override def scoped[A](context: LoggingContext)(fa: Task[A]): Task[A] = ZioLoggingContext.scoped(context)(fa)

  given ZioUIOLocalContext: LocalContext[UIO] = new:
    override def use[A](f: LoggingContext => UIO[A]): UIO[A]            = ZioLoggingContext.use(f)
    override def scoped[A](context: LoggingContext)(fa: UIO[A]): UIO[A] = ZioLoggingContext.scoped(context)(fa)

object LocalContextInstance extends LocalContextInstance
