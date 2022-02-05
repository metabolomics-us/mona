package edu.ucdavis.fiehnlab.mona.core.similarity.math.similarity

import edu.ucdavis.fiehnlab.mona.core.similarity.types.SimpleSpectrum
import org.scalatest.flatspec.AnyFlatSpec
import org.scalatest.matchers.should.Matchers

/**
  * Created by singh on 1/28/2016.
  */
class AbsoluteValueSimilarityTest extends AnyFlatSpec with Matchers {
  val EPSILON: Double = 0.0001


  "Absolute Value Similarity" should "be unity for identical spectra with one ion" in {
    // MassBank EA034401
    val s: SimpleSpectrum = new SimpleSpectrum("", "169.9232:3169812.7")
    s.ions.length shouldEqual 1

    val similarity = new AbsoluteValueSimilarity().compute(s, s)
    similarity shouldBe 1.0 +- EPSILON
  }

  it should "be unity for identical spectra with multiple ions" in {
    val s = new SimpleSpectrum("", "141.0114:5064461 155.0269:44809.4 199.0169:282429")
    s.ions.length shouldEqual 3

    val similarity = new AbsoluteValueSimilarity().compute(s, s)
    similarity shouldBe 1.0 +- EPSILON
  }

  it should "compute correctly for different spectra" in {
    val unknown = new SimpleSpectrum("", "10:100 15:20")
    val library = new SimpleSpectrum("", "10:100 15:25 20:5")
    unknown.ions.length shouldEqual 2
    library.ions.length shouldEqual 3

    val similarity = new AbsoluteValueSimilarity().compute(unknown, library)
    similarity shouldBe 0.96 +- EPSILON
  }
}
