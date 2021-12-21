package edu.ucdavis.fiehnlab.mona.backend.curation.processor.metadata

import java.io.InputStreamReader

import edu.ucdavis.fiehnlab.mona.backend.core.domain.io.json.JSONDomainReader
import edu.ucdavis.fiehnlab.mona.backend.core.domain.{MetaData, Spectrum}
import edu.ucdavis.fiehnlab.mona.backend.curation.util.CommonMetaData
import org.scalatest.wordspec.AnyWordSpec
import org.scalatest.matchers.should.Matchers

class SpectralEntropyTest extends AnyWordSpec with Matchers {
  val reader: JSONDomainReader[Spectrum] = JSONDomainReader.create[Spectrum]

  "this processor" when {
    val processor = new SpectralEntropy
    val exampleRecord: Spectrum = JSONDomainReader.create[Spectrum].read(new InputStreamReader(getClass.getResourceAsStream("/monaRecord.json")))

    "given a spectra" must {
      "correctly calculate the spectrum entropy" in {
        val processedSpectrum = processor.process(exampleRecord.copy())
      }
    }
  }
}
