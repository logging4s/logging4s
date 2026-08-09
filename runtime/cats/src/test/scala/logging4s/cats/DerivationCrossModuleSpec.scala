package logging4s.cats

import org.scalatest.wordspec.AnyWordSpec
import org.scalatest.matchers.should.Matchers

import logging4s.core.Loggable
import logging4s.core.deriving.MaskMode

final case class ExternalUser(userId: Int, cardNumber: String) derives Loggable

class DerivationCrossModuleSpec extends AnyWordSpec, Matchers:

  "derives Loggable from a module outside core" must:
    "compile and produce structured JSON keyed by the decapitalized type name" in:
      Loggable[ExternalUser].key shouldEqual "externalUser"
      Loggable[ExternalUser].json(ExternalUser(1, "4242")) shouldEqual """{"user_id":1,"card_number":"4242"}"""

    "run the field-policy builder outside core too" in:
      val loggable = Loggable.deriving[ExternalUser].mask(_.cardNumber)(MaskMode.KeepLast(2)).derived

      loggable.json(ExternalUser(1, "4242")) shouldEqual """{"user_id":1,"card_number":"**42"}"""
