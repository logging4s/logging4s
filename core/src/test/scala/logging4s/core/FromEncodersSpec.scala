package logging4s.core

import org.scalatest.wordspec.AnyWordSpec
import org.scalatest.matchers.should.Matchers

import logging4s.core.config.{KeyNameStyle, LoggableEncodingConfig}

final case class Contact(firstName: String, lastName: String)

class OpaqueId(val raw: Int)

class FromEncodersSpec extends AnyWordSpec, Matchers:

  private given JsonEncoder[Contact] =
    c => JsonString(s"""{"firstName":"${c.firstName}","lastName":"${c.lastName}"}""")

  "Loggable.fromEncoders" must:
    "take JSON verbatim from the encoder, never applying keyNameStyle to it" in:
      given LoggableEncodingConfig = LoggableEncodingConfig(keyNameStyle = KeyNameStyle.SnakeCase)

      val loggable = Loggable.fromEncoders[Contact]

      loggable.key shouldEqual "contact"
      loggable.json(Contact("John", "Doe")) shouldEqual """{"firstName":"John","lastName":"Doe"}"""

    "derive plain structurally from config when no PlainEncoder is in scope" in:
      given LoggableEncodingConfig = LoggableEncodingConfig()

      Loggable.fromEncoders[Contact].plain(Contact("John", "Doe")) shouldEqual
        "first_name -> (John), last_name -> (Doe)"

    "use a user-provided PlainEncoder for plain when present" in:
      given PlainEncoder[Contact] = c => PlainString(s"${c.firstName} ${c.lastName}")

      Loggable.fromEncoders[Contact].plain(Contact("John", "Doe")) shouldEqual "John Doe"

    "fall back to the JSON string as plain for a non-ADT type without a PlainEncoder" in:
      given JsonEncoder[OpaqueId] = id => JsonString(id.raw.toString)

      val loggable = Loggable.fromEncoders[OpaqueId]

      loggable.key shouldEqual "opaqueId"
      loggable.json(new OpaqueId(7)) shouldEqual "7"
      loggable.plain(new OpaqueId(7)) shouldEqual "7"
