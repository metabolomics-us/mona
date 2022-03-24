package edu.ucdavis.fiehnlab.mona.core.similarity.math.similarity

import edu.ucdavis.fiehnlab.mona.core.similarity.types.SimpleSpectrum
import org.scalatest.flatspec.AnyFlatSpec
import org.scalatest.matchers.should.Matchers

/**
  * Created by singh on 1/28/2016.
  */
class CompositeSimilarityTest extends AnyFlatSpec with Matchers {
  val EPSILON: Double = 0.0001


  "Composite Similarity" should "be unity for identical spectra with one ion" in {
    // MassBank EA034401
    val s: SimpleSpectrum = new SimpleSpectrum("", "169.9232:3169812.7")
    s.ions.length shouldEqual 1

    val similarity = new CompositeSimilarity().compute(s, s)
    similarity shouldBe 1.0 +- EPSILON
  }

  it should "be unity for identical spectra with multiple ions" in {
    val s = new SimpleSpectrum("", "141.0114:5064461 155.0269:44809.4 199.0169:282429")
    s.ions.length shouldEqual 3

    val similarity = new CompositeSimilarity().compute(s, s)
    similarity shouldBe 1.0 +- EPSILON
  }

  it should "be unity for identical spectra with multiple ions, which are nominal" in {
    val s = new SimpleSpectrum("", "110:123 112:23 113:23 123:32 125:100")
    s.ions.length shouldEqual 5

    val similarity = new CompositeSimilarity().compute(s, s)
    similarity shouldBe 1.0 +- EPSILON
  }

  it should "compute correctly for different spectra" in {
    val unknown = new SimpleSpectrum("", "10:100 15:20")
    val library = new SimpleSpectrum("", "10:100 15:25 20:5")
    unknown.ions.length shouldEqual 2
    library.ions.length shouldEqual 3

    val similarity = new CompositeSimilarity().compute(unknown, library)
    similarity shouldBe 0.9476 +- EPSILON
  }

  it should "be zero for orthogonal spectra" in {
    val unknown = new SimpleSpectrum("", "10:100 15:20")
    val library = new SimpleSpectrum("", "12:100 17:25 20:5")
    unknown.ions.length shouldEqual 2
    library.ions.length shouldEqual 3

    val similarity = new CompositeSimilarity().compute(unknown, library)
    similarity shouldBe 0.0 +- EPSILON
  }

  it should "remove precursor ions" in {
    val unknown = new SimpleSpectrum("", "10:100 15:20 16:5", 16.0, null)
    val library = new SimpleSpectrum("", "10:100 15:25 20:5 25:2", 25.0, null)
    unknown.ions.length shouldEqual 3
    library.ions.length shouldEqual 4

    val similarity = new CompositeSimilarity().compute(unknown, library, true)
    similarity shouldBe 0.948868778280543 +- EPSILON
  }
}
