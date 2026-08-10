package logging4s.core

import org.scalatest.wordspec.AnyWordSpec
import org.scalatest.matchers.should.Matchers

class PluralizedCollectionKeySpec extends AnyWordSpec, Matchers:

  final case class User(name: String)
  final case class Bus(id: Int)
  final case class Entry(text: String)

  private given Loggable[User]  = Loggable.make[User]("user")(u => JsonString.quoted(u.name), u => PlainString(u.name))
  private given Loggable[Bus]   = Loggable.make[Bus]("bus")(b => JsonString(b.id.toString), b => PlainString(b.id.toString))
  private given Loggable[Entry] = Loggable.make[Entry]("entry")(e => JsonString.quoted(e.text), e => PlainString(e.text))

  "A collection Loggable key" must:
    "pluralize the element key so the single (object) and collection (array) forms never collide" in:
      Loggable[String].key shouldEqual "string"

      Loggable[List[String]].key shouldEqual "strings"
      Loggable[Vector[String]].key shouldEqual "strings"
      Loggable[Set[String]].key shouldEqual "strings"
      Loggable[Seq[String]].key shouldEqual "strings"

    "pluralize a custom element key (user -> users)" in:
      Loggable[User].key shouldEqual "user"
      Loggable[List[User]].key shouldEqual "users"
      Loggable[Seq[User]].key shouldEqual "users"

    "keep a key ending in 's' distinct from its collection (bus -> buses)" in:
      Loggable[Bus].key shouldEqual "bus"
      Loggable[Seq[Bus]].key shouldEqual "buses"
      Loggable[List[Bus]].key shouldEqual "buses"

    "pluralize a consonant + 'y' key correctly (entry -> entries)" in:
      Loggable[Entry].key shouldEqual "entry"
      Loggable[Seq[Entry]].key shouldEqual "entries"

    "pluralize the Map key derived from the tuple key" in:
      Loggable[Map[String, Int]].key shouldEqual "string_ints"

    "keep every nesting level distinct so array-of-array never collides with array" in:
      Loggable[Seq[Seq[String]]].key shouldEqual "stringses"
      Loggable[List[Set[User]]].key shouldEqual "userses"
