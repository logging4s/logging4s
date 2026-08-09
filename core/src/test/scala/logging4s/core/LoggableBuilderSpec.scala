package logging4s.core

import org.scalatest.wordspec.AnyWordSpec
import org.scalatest.matchers.should.Matchers

import logging4s.core.deriving.MaskMode

class LoggableBuilderSpec extends AnyWordSpec, Matchers:

  "Loggable.deriving builder" must:
    "hide a field from both json and plain" in:
      val loggable = Loggable.deriving[Account].hide(_.cardNumber).derived

      loggable.json(Account(1, "4242")) shouldEqual """{"user_id":1}"""
      loggable.plain(Account(1, "4242")) shouldEqual "user_id -> (1)"

    "mask a field, keeping the last N chars" in:
      val loggable = Loggable.deriving[Account].mask(_.cardNumber)(MaskMode.KeepLast(4)).derived

      loggable.json(Account(1, "1234567890")) shouldEqual """{"user_id":1,"card_number":"******7890"}"""
      loggable.plain(Account(1, "1234567890")) shouldEqual "user_id -> (1), card_number -> (******7890)"

    "mask a field fully" in:
      val loggable = Loggable.deriving[Account].mask(_.cardNumber)(MaskMode.Full).derived

      loggable.json(Account(7, "42")) shouldEqual """{"user_id":7,"card_number":"**"}"""

    "rename a field's key" in:
      val loggable = Loggable.deriving[Account].rename(_.userId, "uid").derived

      loggable.json(Account(7, "4242")) shouldEqual """{"uid":7,"card_number":"4242"}"""

    "unembed a nested product's fields into the parent JSON" in:
      val loggable = Loggable.deriving[Wrapper].unembed(_.owner).derived

      loggable.json(Wrapper(Account(1, "4242"))) shouldEqual """{"user_id":1,"card_number":"4242"}"""

    "combine several policies" in:
      val loggable =
        Loggable.deriving[Account].rename(_.userId, "uid").mask(_.cardNumber)(MaskMode.KeepFirst(1)).derived

      loggable.json(Account(7, "4242")) shouldEqual """{"uid":7,"card_number":"4***"}"""
