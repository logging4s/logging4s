package logging4s.examples

import java.util.UUID
import cats.Show
import io.circe.Encoder
import io.circe.generic.semiauto.deriveEncoder
import logging4s.core.Loggable
import logging4s.cats.instances.given
import logging4s.json.circe.instances.given

final case class User(id: UUID, name: String, age: Int)

object User:

  given Show[User]     = user => s"id=${user.id}, name=${user.name}, age=${user.age}"
  given Encoder[User]  = deriveEncoder
  given Loggable[User] = Loggable.make("user")
