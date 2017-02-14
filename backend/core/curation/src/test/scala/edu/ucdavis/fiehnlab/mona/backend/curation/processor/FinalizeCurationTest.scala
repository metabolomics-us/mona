package edu.ucdavis.fiehnlab.mona.backend.curation.processor

import java.io.InputStreamReader

import edu.ucdavis.fiehnlab.mona.backend.core.domain.Spectrum
import edu.ucdavis.fiehnlab.mona.backend.core.domain.io.json.JSONDomainReader
import org.scalatest.WordSpec

/**
  * Created by sajjan on 2/14/17.
  */
class FinalizeCurationTest extends WordSpec {

  val reader: JSONDomainReader[Spectrum] = JSONDomainReader.create[Spectrum]

  "this processor" when {

    val processor = new FinalizeCuration

    val input = new InputStreamReader(getClass.getResourceAsStream("/monaRecord.json"))
    val spectrumGiven: Spectrum = reader.read(input)

    "given a spectra" must {

      val processedSpectrum = processor.process(spectrumGiven)

      "remove the computed metadata for the spectrum" in {
        assert(processedSpectrum.metaData.exists(_.name == "Last Auto-Curation"))
      }
    }
  }
}