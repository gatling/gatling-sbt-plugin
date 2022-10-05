package unit

import org.scalatest.flatspec.AnyFlatSpec
import org.scalatest.matchers.should.Matchers

class Spec extends AnyFlatSpec with Matchers {
  "1" should "be 1" in {
    1 shouldBe 1
  }
}
