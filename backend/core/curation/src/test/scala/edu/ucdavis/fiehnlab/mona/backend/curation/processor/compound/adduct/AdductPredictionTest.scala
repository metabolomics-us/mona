package edu.ucdavis.fiehnlab.mona.backend.curation.processor.compound.adduct

import java.io.InputStreamReader

import edu.ucdavis.fiehnlab.mona.backend.core.domain.Spectrum
import edu.ucdavis.fiehnlab.mona.backend.core.domain.io.json.JSONDomainReader
import org.scalatest.{Matchers, WordSpec}

/**
  * Created by sajjan on 3/21/16.
  */
class AdductPredictionTest extends WordSpec with Matchers {

  val reader: JSONDomainReader[Spectrum] = JSONDomainReader.create[Spectrum]

  "this processor" when {
    val processor = new AdductPrediction

    val exampleRecord: Spectrum = JSONDomainReader.create[Spectrum].read(new InputStreamReader(getClass.getResourceAsStream("/monaRecord.json")))

    "given a spectra" must {
      "correctly calculate the precursor type" in {
        val modifiedRecord: Spectrum = exampleRecord.copy(
          metaData = exampleRecord.metaData.filter(x => !x.computed && x.name != "precursor type")
        )
        val processedSpectrum = processor.process(modifiedRecord)

        assert(processedSpectrum.metaData.exists(x => x.name == "precursor type" && x.value == "[M+H]+" && x.computed))
      }

      "correctly calculate the precursor m/z" in {
        val modifiedRecord: Spectrum = exampleRecord.copy(
          metaData = exampleRecord.metaData.filter(x => !x.computed && x.name != "precursor m/z")
        )
        val processedSpectrum = processor.process(modifiedRecord)

        assert(processedSpectrum.metaData.exists(x => x.name == "precursor m/z" && x.computed))
      }
    }
  }
}
