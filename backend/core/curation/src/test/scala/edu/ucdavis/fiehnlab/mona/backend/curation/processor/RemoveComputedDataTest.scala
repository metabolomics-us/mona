package edu.ucdavis.fiehnlab.mona.backend.curation.processor

import java.io.InputStreamReader

import edu.ucdavis.fiehnlab.mona.backend.core.domain.Spectrum
import edu.ucdavis.fiehnlab.mona.backend.core.domain.io.json.JSONDomainReader
import org.scalatest.WordSpec

/**
  * Created by wohlgemuth on 3/11/16.
  */
class RemoveComputedDataTest extends WordSpec {

  val reader: JSONDomainReader[Spectrum] = JSONDomainReader.create[Spectrum]

  "this processor" when {
    val processor = new RemoveComputedData

    val input = new InputStreamReader(getClass.getResourceAsStream("/monaRecord.json"))
    val spectrumGiven: Spectrum = reader.read(input)

    "given a spectra" must {
      val processedSpectrum = processor.process(spectrumGiven)

      "remove the computed metadata for the spectrum" in {
        assert(processedSpectrum.metaData.forall(!_.computed))
      }

      "remove the computed metadata for each compound" in {
        processedSpectrum.compound.foreach { compound =>
          assert(compound.metaData.forall(!_.computed))
        }
      }

      "remove the computed names for each compound" in {
        processedSpectrum.compound.foreach { compound =>
          assert(compound.names.forall(!_.computed))
        }
      }

      "remove the computed tags for the spectrum" in {
        assert(processedSpectrum.tags.forall(!_.ruleBased))
      }

      "remove the computed tags for each compound" in {
        processedSpectrum.compound.foreach { compound =>
          assert(compound.tags.forall(!_.ruleBased))
        }
      }
    }
  }
}
