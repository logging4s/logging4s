package logging4s.benchmarks.backends

import com.github.plokhotnyuk.jsoniter_scala.core.JsonValueCodec
import com.github.plokhotnyuk.jsoniter_scala.macros.JsonCodecMaker

import logging4s.core.Loggable

import logging4s.json.jsoniter.JsoniterInstances.given

object domain:

  final case class Address(street: String, city: String, zip: Int) derives Loggable

  final case class Event(
      id: Long,
      name: String,
      count: Int,
      ratio: Double,
      active: Boolean,
      code: Char,
      tags: List[String],
      address: Address,
  )

  val sample: Event = Event(
    id = 42L,
    name = "checkout completed",
    count = 7,
    ratio = 0.85,
    active = true,
    code = 'A',
    tags = List("payment", "eu", "priority"),
    address = Address("221B Baker Street", "London", 12345),
  )

  given JsonValueCodec[Event] = JsonCodecMaker.make

  val derived: Loggable[Event]  = Loggable.derived
  val jsoniter: Loggable[Event] = Loggable.fromEncoders
