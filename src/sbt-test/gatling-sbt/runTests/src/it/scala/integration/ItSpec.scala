package integration

import org.scalatest.flatspec.AnyFlatSpec
import org.scalatest.matchers.should.Matchers

class ItSpec extends AnyFlatSpec with Matchers {
  "1" should "be 1" in {
    1 shouldBe 1
  }
}
