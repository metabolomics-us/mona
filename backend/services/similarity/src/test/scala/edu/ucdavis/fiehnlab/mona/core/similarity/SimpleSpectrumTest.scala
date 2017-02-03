package edu.ucdavis.fiehnlab.mona.core.similarity

import edu.ucdavis.fiehnlab.mona.core.similarity.types.{Ion, SimpleSpectrum}
import org.scalatest.{Matchers, WordSpec}

/**
  * Created by sajjan on 12/14/16.
  */
class SimpleSpectrumTest extends WordSpec with Matchers {
  val spectrum: SimpleSpectrum = new SimpleSpectrum("", "100:100 102:32 103:22 104:33 105:55 106:99 107:3 108:80")

  "A created SimpleSpectrum" should {
    "have 8 ions and 8 top ions" in {
      spectrum.ions.length shouldEqual 8
      spectrum.topIons.length shouldEqual 8
    }

    "have a minimum ion of m/z = 100 and maximum ion of m/z = 108" in {
      spectrum.minimumIon shouldEqual Ion(100, 100)
      spectrum.maximumIon shouldEqual Ion(108, 80)
    }

    "have a base peak of m/z = 100" in {
      spectrum.basePeak shouldEqual Ion(100, 100)
    }

    "have 4 high intensity ions" in {
      spectrum.highIntensityIons.length shouldEqual 4
    }
  }
}
