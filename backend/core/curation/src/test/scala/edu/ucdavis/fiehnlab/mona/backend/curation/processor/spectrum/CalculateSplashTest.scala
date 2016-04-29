package edu.ucdavis.fiehnlab.mona.backend.curation.processor.spectrum

import java.io.InputStreamReader

import edu.ucdavis.fiehnlab.mona.backend.core.domain.Spectrum
import edu.ucdavis.fiehnlab.mona.backend.core.domain.io.json.JSONDomainReader
import org.scalatest.WordSpec

/**
  * Created by sajjan on 3/21/16.
  */
class CalculateSplashTest extends WordSpec {

  val reader = JSONDomainReader.create[Spectrum]

  "this processor" when {

    val processor = new CalculateSplash

    val input = new InputStreamReader(getClass.getResourceAsStream("/monaRecord.json"))

    val spectrumGiven: Spectrum = reader.read(input)

    "given a spectra" must {

      val processedSpectrum = processor.process(spectrumGiven)

      "correctly calculate the SPLASH" in {
        assert(processedSpectrum.splash.splash == "splash10-0z50000000-9c8c58860a0fadd33800")
      }
    }
  }
}
