package edu.ucdavis.fiehnlab.mona.backend.curation.processor.spectrum

import java.io.InputStreamReader

import edu.ucdavis.fiehnlab.mona.backend.core.domain.Spectrum
import edu.ucdavis.fiehnlab.mona.backend.core.domain.io.json.JSONDomainReader
import org.scalatest.WordSpec

/**
  * Created by sajjan on 4/21/16.
  */
class CalculateSpectralQualityTest extends WordSpec {
  val reader = JSONDomainReader.create[Spectrum]

  "this processor" when {
    val processor = new CalculateSpectralQuality

    val input = new InputStreamReader(getClass.getResourceAsStream("/monaRecord.json"))
    val spectrumGiven: Spectrum = reader.read(input)

    "given a spectra" must {
      val processedSpectrum = processor.process(spectrumGiven)

    }
  }
}
