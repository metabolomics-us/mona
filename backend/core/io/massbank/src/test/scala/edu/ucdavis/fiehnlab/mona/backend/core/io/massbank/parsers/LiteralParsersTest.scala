package edu.ucdavis.fiehnlab.mona.backend.core.io.massbank.parsers

import org.scalatest._
import org.scalatest.prop.GeneratorDrivenPropertyChecks

import scala.util.Try

class LiteralParsersTest extends WordSpec with GeneratorDrivenPropertyChecks with Matchers with LiteralParsers {

  "`anyString` literal parser" must {
    "parse any non-empty string" in {
      forAll(Generators.validString) { s => parseAll(anyString, s) shouldBe a[Success[_]] }
      forAll(Generators.validInt) { i => parseAll(integer, i.toString) shouldBe a[Success[_]] }
      forAll(Generators.validDouble) { d => parseAll(double, d.toString) shouldBe a[Success[_]] }
    }

    "reject non-empty strings" in { parseAll(anyString, "") shouldBe a[Failure] }
  }

  "`integer` literal parser" must {
    "parse valid integers" in {
      forAll(Generators.validInt) { i => parseAll(integer, i.toString) shouldBe a[Success[_]] }
    }

    "reject strings and doubles" in {
      val invalidInt = Generators.validString suchThat (s => Try(s.toInt).isFailure)
      forAll(invalidInt) { s: String => parseAll(integer, s) shouldBe a[Failure] }
      forAll(Generators.validDouble) { d => parseAll(integer, d.toString) shouldBe a[Failure] }
    }
  }

  "`double` literal parser" must {
    "parse valid doubles" in {
      forAll(Generators.validDouble) { d => parseAll(double, d.toString) shouldBe a[Success[_]] }
      forAll(Generators.validInt) { i => parseAll(double, i.toString) shouldBe a[Success[_]] }
    }

    "reject strings" in {
      val invalidDouble = Generators.validString.suchThat(s => Try(s.toDouble).isFailure)
      forAll(invalidDouble) { s => parseAll(double, s) shouldBe a[Failure] }
    }
  }

  "`date` literal parser" must {
    "parse valid dates" in {
      forAll(Generators.validDate) { s => parseAll(date, s) shouldBe a[Success[_]] }
    }
  }

  "`subtag` literal parser" should {
    "parse valid string pairs" in {
      forAll(Generators.validTag) { s =>
        val result = parseAll(subtag, s)
        result shouldBe a[Success[_]]
        result.get._1 shouldBe s.split(" ")(0)
      }
    }
  }

  "`peak` literal parser" should {
    "parse and correctly identify peaks" in {
      val complete = "m/z int. rel.int."
      forAll(Generators.completePeak) { s =>
        val result = parseAll(peakTriple, s)
        result shouldBe a[Success[_]]
      }
    }
  }
}
