package logging4s.zio

import zio.{UIO, ZIO}

import logging4s.core.Delay

trait UIOToDelayInstance:

  given ZioUIODelay: Delay[UIO] = new:
    override def delay[A](a: => A): UIO[A] = ZIO.succeed(a)
    override val unit: UIO[Unit]           = ZIO.unit

object UIOToDelayInstance extends UIOToDelayInstance
