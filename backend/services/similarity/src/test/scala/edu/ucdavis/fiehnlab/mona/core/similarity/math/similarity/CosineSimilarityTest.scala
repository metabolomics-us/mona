package edu.ucdavis.fiehnlab.mona.core.similarity.math.similarity

import edu.ucdavis.fiehnlab.mona.core.similarity.types.SimpleSpectrum
import org.scalatest.{FlatSpec, Matchers}

/**
  * Created by singh on 1/28/2016.
  */
class CosineSimilarityTest extends FlatSpec with Matchers {
  val EPSILON: Double = 0.0001


  "Cosine Similarity" should "be unity for identical spectra with one ion" in {
    // MassBank EA034401
    val s: SimpleSpectrum = new SimpleSpectrum("", "169.9232:3169812.7")
    s.ions.length shouldEqual 1

    val similarity = new CosineSimilarity().compute(s, s)
    similarity shouldBe 1.0 +- EPSILON
  }

  it should "be unity for identical spectra with multiple ions" in {
    val s = new SimpleSpectrum("", "141.0114:5064461 155.0269:44809.4 199.0169:282429")
    s.ions.length shouldEqual 3

    val similarity = new CosineSimilarity().compute(s, s)
    similarity shouldBe 1.0 +- EPSILON
  }

  it should "be unity for identical spectra with multiple ions, which are nominal" in {
    val s = new SimpleSpectrum("", "110:123 112:23 113:23 123:32 125:100")
    s.ions.length shouldEqual 5

    val similarity = new CosineSimilarity().compute(s, s)
    similarity shouldBe 1.0 +- EPSILON
  }

  it should "compute correctly for different spectra" in {
    val unknown = new SimpleSpectrum("", "10:100 15:20")
    val library = new SimpleSpectrum("", "10:100 15:25 20:5")
    unknown.ions.length shouldEqual 2
    library.ions.length shouldEqual 3

    val similarity = new CosineSimilarity().compute(unknown, library)
    similarity shouldBe 0.9977 +- EPSILON
  }

  it should "be zero for orthogonal spectra" in {
    val unknown = new SimpleSpectrum("", "10:100 15:20")
    val library = new SimpleSpectrum("", "12:100 17:25 20:5")
    unknown.ions.length shouldEqual 2
    library.ions.length shouldEqual 3

    val similarity = new CosineSimilarity().compute(unknown, library)
    similarity shouldBe 0.0 +- EPSILON
  }
}
