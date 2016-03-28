package edu.ucdavis.fiehnlab.mona.backend.curation.processor.instrument

import java.io.InputStreamReader

import edu.ucdavis.fiehnlab.mona.backend.core.domain.Spectrum
import edu.ucdavis.fiehnlab.mona.backend.core.domain.io.json.JSONDomainReader
import edu.ucdavis.fiehnlab.mona.backend.curation.common.CommonTags
import edu.ucdavis.fiehnlab.mona.backend.curation.processor.cts.instrument.IdentifyChromatography
import org.scalatest.WordSpec

/**
  * Created by sajjan on 3/21/16.
  */
class IdentifyChromotographyTest extends WordSpec {

  val reader = JSONDomainReader.create[Spectrum]

  "this processor" when {

    val processor = new IdentifyChromatography

    val input = new InputStreamReader(getClass.getResourceAsStream("/monaRecord.json"))

    val spectrumGiven: Spectrum = reader.read(input)

    "given a spectra" must {

      val processedSpectrum = processor.process(spectrumGiven)

      "identify spectrum as GC/MS" in {
        assert(processedSpectrum.tags.exists(_.text == CommonTags.GCMS_SPECTRUM))
      }

      "not identify spectrum as LC/MS" in {
        assert(processedSpectrum.tags.forall(_.text != CommonTags.LCMS_SPECTRUM))
      }
    }
  }
}
