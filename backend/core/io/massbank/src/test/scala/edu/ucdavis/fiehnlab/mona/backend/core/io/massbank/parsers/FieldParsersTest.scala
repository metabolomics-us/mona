package edu.ucdavis.fiehnlab.mona.backend.core.io.massbank.parsers

import org.scalatest.wordspec.AnyWordSpec
import org.scalatest.matchers.should.Matchers
import org.scalatestplus.scalacheck.ScalaCheckDrivenPropertyChecks

class FieldParsersTest extends AnyWordSpec with ScalaCheckDrivenPropertyChecks with Matchers with FieldParsers {
  "`numPeakField` parser" should {
    "accept integer inputs" in {
      forAll(Generators.validInt) { i: Int =>
        val result = parse(numPeakField("PK$NUM_PEAK"), "PK$NUM_PEAK: " + i.toString)
        result shouldBe a[Success[_]]
        result.get shouldBe Some(i)
      }
    }

    "reject non-integer inputs" in {
      forAll(Generators.validString) { s: String =>
        val result = parseAll(numPeakField("PK$NUM_PEAK"), "PK$NUM_PEAK: " + s)
        result shouldBe a[Success[_]]
        result.get shouldBe None
      }
    }
  }
}
