package edu.ucdavis.fiehnlab.mona.backend.curation.processor

import java.io.{InputStreamReader, FileReader}

import edu.ucdavis.fiehnlab.mona.backend.core.domain.Types.Spectrum
import edu.ucdavis.fiehnlab.mona.backend.core.domain.io.json.JSONDomainReader
import org.scalatest.WordSpec

/**
  * Created by wohlgemuth on 3/11/16.
  */
class RemoveComputedMetaDataTest extends WordSpec {

  val reader = JSONDomainReader.create[Spectrum]

  "this processor" when {

    val processor = new RemoveComputedMetaData

    val input = new InputStreamReader(getClass.getResourceAsStream("/monaRecord.json"))

    val spectrumGiven: Spectrum = reader.read(input)

    "given a spectra" must {

      val processedSpectrum = processor.process(spectrumGiven)

      "remove the computed metadata for spectra" in {

        processedSpectrum.metaData.foreach { metaData =>
          assert(metaData.computed == false)
        }
      }

      "remove the computed metadata for biological compound" in {

        processedSpectrum.biologicalCompound.metaData.foreach { metaData =>
          assert(metaData.computed == false)
        }

      }

      "remove the computed metadata for chemical compound" in {

        processedSpectrum.chemicalCompound.metaData.foreach { metaData =>
          assert(metaData.computed == false)
        }

      }

      "and remove the predicted compound" in {
        assert(processedSpectrum.predictedCompound == null)
      }

    }
  }
}
