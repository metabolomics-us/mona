package edu.ucdavis.fiehnlab.mona.backend.curation.processor

import com.typesafe.scalalogging.LazyLogging
import edu.ucdavis.fiehnlab.mona.backend.core.domain.Spectrum

import java.io.InputStreamReader
import edu.ucdavis.fiehnlab.mona.backend.core.domain.io.json.JSONDomainReader
import org.scalatest.wordspec.AnyWordSpec

/**
  * Created by sajjan on 2/14/17.
  */
class FinalizeCurationTest extends AnyWordSpec with LazyLogging{

  val reader: JSONDomainReader[Spectrum] = JSONDomainReader.create[Spectrum]

  "this processor" when {
    val processor = new FinalizeCuration

    val input = new InputStreamReader(getClass.getResourceAsStream("/monaRecord.json"))
    val spectrumGiven: Spectrum = reader.read(input)

    "given a spectra" must {
      val processedSpectrum = processor.process(new Spectrum(spectrumGiven))

      "add lastCurated field to spectrum" in {
        assert(spectrumGiven.getLastCurated == null)
        assert(processedSpectrum.getLastCurated != null)
      }
    }
  }
}
