package edu.ucdavis.fiehnlab.mona.core.similarity.math.similarity

import com.typesafe.scalalogging.LazyLogging
import org.scalatest.wordspec.AnyWordSpec
import edu.ucdavis.fiehnlab.mona.core.similarity.util.SpectrumUtils
import org.scalatest.matchers.should.Matchers

class EntropySimilarityTest extends AnyWordSpec with Matchers with LazyLogging {

  val ms2_da = 0.5
  val ms2_ppm = 0.0

  // val spec1 = "80.765000:1760.65 81.070000:6983.60 95.085000:19192.86 105.065000:1994.32 105.070000:2730.00 107.950000:1894.58 112.850000:2084.40 123.055000:4453.16 123.080000:401874.31"
  // val spec2 = "80.765000:1760.65 82.070000:6983.60 95.085000:19192.86 105.065000:1994.32 105.070000:2730.00 107.950000:1894.58 112.850000:2084.40 123.055000:4453.16 123.080000:401874.31"

  val spec1 = "42.0397:3.19 42.0422:107.15 42.0490:16.35 42.9119:6.97 43.0328:11.55 43.0375:1340.42 43.0429:30.73 43.9227:51.07 43.9274:19.74 43.9304:33.73 43.9371:7.53 43.9456:13.87 44.0494:0.74 44.0585:9548.23 44.0610:6.82 44.0658:4.32 44.6138:4.18 57.0487:5.15 70.0755:3.09 72.0490:18.04 72.0534:91.16 72.0556:9.42 72.0623:7.01 86.0726:55.58 86.0783:535.99 86.0821:76.09 86.0874:15.83 87.0363:4.88 113.7295:3.68 114.0584:19.74 114.0645:19.93 114.0687:490.79 114.0735:2741.10 114.0767:210.33 114.0825:13.64 114.0984:6.61 114.1007:2.70"
  val spec2 = "42.0325:16.73 42.0412:0.81 43.0275:212.41 43.0307:319.10 43.9214:11.28 44.0483:407.04 44.0506:2562.51 44.0608:1.98 44.3000:1.67 44.3588:3.97 44.3682:5.85 44.5342:2.51 45.0477:13.12 72.0424:32.70 86.0721:55.00 86.0762:18.14 114.0533:30.85 114.0629:21.33 114.0673:616.22"

  "EntropySimilarity" should {
    "calculate entropy same spectrum" in {
      val entropy = new EntropySimilarity()
      val entropy11 = entropy.entropy_similarity(SpectrumUtils.toArray(spec1),
        SpectrumUtils.toArray(spec1), ms2_da)
      val wentropy11 = entropy.weighted_entropy_similarity(SpectrumUtils.toArray(spec1),
        SpectrumUtils.toArray(spec1), ms2_da)

      logger.info(s"Entropy (spec1, spec1): ${entropy11}")
      logger.info(s"Weighted entropy (spec1, spec1): ${wentropy11}")

      entropy11 shouldBe 1.0
      wentropy11 shouldBe 1.0
    }

    "calculate entropy different spectra" in {
      val entropy = new EntropySimilarity()
      val entropy12 = entropy.entropy_similarity(SpectrumUtils.toArray(spec1),
        SpectrumUtils.toArray(spec2), ms2_da)
      val wentropy12 = entropy.weighted_entropy_similarity(SpectrumUtils.toArray(spec1),
        SpectrumUtils.toArray(spec2), ms2_da)

      logger.info(s"Entropy (spec1, spec2): ${entropy12}")
      logger.info(s"Weighted entropy (spec1, spec2): ${wentropy12}")

      entropy12 should be < 1.0D
      wentropy12 should be < 1.0D
    }

    "Test 1, should be 0.3830598855325621, 0.40934370120266905" in {
      val entropy = new EntropySimilarity()
      val spec_a: Array[Array[Double]] = Array(Array(80.0, 30.0), Array(100.001, 10.0), Array(100.002, 10.0), Array(200.0, 80.0), Array(300.0, 20.0))
      val spec_b: Array[Array[Double]] = Array(Array(100.002, 30.0), Array(200.0, 0.0), Array(210.0, 20.0), Array(100.0, 30.0), Array(300.0, 70.0), Array(400.0, 70.0))

      val entropy12 = entropy.entropy_similarity(spec_a, spec_b, ms2_da, ms2_ppm)
      val wentropy12 = entropy.weighted_entropy_similarity(spec_a, spec_b, ms2_da, ms2_ppm)
      logger.info(s"Entropy (spec1, spec2): ${entropy12}")
      logger.info(s"Weighted entropy (spec1, spec2): ${wentropy12}")
      entropy12 shouldBe 0.3830598855325621D +- 1.0e-6
      wentropy12 shouldBe 0.40934370120266905D +- 1.0e-6
    }

    "Test 2, should be 0.5609400966627509, 0.620750035624648" in {
      val entropy = new EntropySimilarity()
      val spec_a = Array(Array(80.0, 30.0), Array(100.001, 10.0), Array(100.002, 100.0), Array(100.003, 10.0), Array(200.0, 80.0), Array(300.0, 20.0))
      val spec_b = Array(Array(100.0, 30.0), Array(200.0, 0.0), Array(300.0, 70.0))

      val entropy12 = entropy.entropy_similarity(spec_a, spec_b, ms2_da, ms2_ppm)
      val wentropy12 = entropy.weighted_entropy_similarity(spec_a, spec_b, ms2_da, ms2_ppm)
      logger.info(s"Entropy (spec1, spec2): ${entropy12}")
      logger.info(s"Weighted entropy (spec1, spec2): ${wentropy12}")
      entropy12 shouldBe 0.5609400966627509D +- 1.0e-6
      wentropy12 shouldBe 0.620750035624648D +- 1.0e-6
    }

    "Test 3, should be 0.0, 0.0" in {
      val entropy = new EntropySimilarity()
      val spec_a = Array(Array(100.002, 0.0), Array(80.0, 0.0), Array(100.001, 0.0), Array(200.0, 0.0), Array(300.0, 0.0))
      val spec_b = Array(Array(100.0, 0.0), Array(100.002, 0.0), Array(200.0, 0.0), Array(210.0, 0.0), Array(300.0, 0.0), Array(400.0, 0.0))

      val entropy12 = entropy.entropy_similarity(spec_a, spec_b, ms2_da, ms2_ppm)
      val wentropy12 = entropy.weighted_entropy_similarity(spec_a, spec_b, ms2_da, ms2_ppm)
      logger.info(s"Entropy (spec1, spec2): ${entropy12}")
      logger.info(s"Weighted entropy (spec1, spec2): ${wentropy12}")
      entropy12 shouldBe 0.0D
      wentropy12 shouldBe 0.0D
    }

    "Test 4, should be 1.0, 1.0" in {
      val entropy = new EntropySimilarity()
      val spec_a = Array(Array(100.0, 10.0))
      val spec_b = Array(Array(100.0, 10.0), Array(200.0, 0.0))

      val entropy12 = entropy.entropy_similarity(spec_a, spec_b, ms2_da, ms2_ppm)
      val wentropy12 = entropy.weighted_entropy_similarity(spec_a, spec_b, ms2_da, ms2_ppm)
      logger.info(s"Entropy (spec1, spec2): ${entropy12}")
      logger.info(s"Weighted entropy (spec1, spec2): ${wentropy12}")
      entropy12 shouldBe 1.0D
      wentropy12 shouldBe 1.0D
    }
  }
}
