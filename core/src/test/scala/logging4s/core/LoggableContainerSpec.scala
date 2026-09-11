package logging4s.core

import scala.collection.mutable.ArrayBuffer
import scala.collection.immutable.{Queue, SortedSet}

import org.scalatest.wordspec.AnyWordSpec
import org.scalatest.matchers.should.Matchers

class LoggableContainerSpec extends AnyWordSpec, Matchers:

  "The low-priority container instance" must:
    "cover a collection that has no instance of its own, keyed by the pluralized element key" in:
      Loggable[Queue[Int]].json(Queue(1, 2)) shouldEqual "[1,2]"
      Loggable[Queue[Int]].key shouldEqual ValueKey("ints")

    "cover an abstract collection type, which an invariant Loggable[Seq] cannot" in:
      Loggable[Iterable[Int]].json(Iterable(1)) shouldEqual "[1]"
      Loggable[IndexedSeq[Int]].json(IndexedSeq(1)) shouldEqual "[1]"

    "cover a subtype of a type that does have its own instance" in:
      Loggable[SortedSet[Int]].json(SortedSet(2, 1)) shouldEqual "[1,2]"

    "cover mutable collections" in:
      Loggable[ArrayBuffer[Int]].json(ArrayBuffer(1, 2)) shouldEqual "[1,2]"

    "lose to the explicit instances, so Set keeps its sorted rendering" in:
      Loggable[Set[Int]].json(Set(2, 1)) shouldEqual "[1,2]"
      Loggable[List[Int]].json(List(2, 1)) shouldEqual "[2,1]"
