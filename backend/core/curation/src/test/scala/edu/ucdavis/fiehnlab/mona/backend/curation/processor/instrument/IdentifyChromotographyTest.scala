package edu.ucdavis.fiehnlab.mona.backend.curation.processor.instrument

import java.io.InputStreamReader

import edu.ucdavis.fiehnlab.mona.backend.core.domain.Spectrum
import edu.ucdavis.fiehnlab.mona.backend.core.domain.io.json.JSONDomainReader
import edu.ucdavis.fiehnlab.mona.backend.curation.util.CommonTags
import org.scalatest.WordSpec

/**
  * Created by sajjan on 3/21/16.
  */
class IdentifyChromotographyTest extends WordSpec {

  val reader: JSONDomainReader[Spectrum] = JSONDomainReader.create[Spectrum]

  "this processor" when {
    val processor = new IdentifyChromatography

    "given an LC/MS spectrum" must {
      val lcmsRecord = new InputStreamReader(getClass.getResourceAsStream("/monaRecord.json"))
      val spectrum: Spectrum = reader.read(lcmsRecord)
      val processedSpectrum = processor.process(spectrum)

      "identify spectrum as LC/MS" in {
        assert(processedSpectrum.tags.exists(_.text == CommonTags.LCMS_SPECTRUM))
      }

      "not identify spectrum as GC/MS or CE/MS" in {
        assert(processedSpectrum.tags.forall(_.text != CommonTags.GCMS_SPECTRUM))
        assert(processedSpectrum.tags.forall(_.text != CommonTags.CEMS_SPECTRUM))
      }
    }

    "given an GC/MS spectrum" must {
      val gcmsRecord = new InputStreamReader(getClass.getResourceAsStream("/gcmsRecord.json"))
      val spectrum: Spectrum = reader.read(gcmsRecord)
      val processedSpectrum = processor.process(spectrum)

      "identify spectrum as GC/MS" in {
        assert(processedSpectrum.tags.exists(_.text == CommonTags.GCMS_SPECTRUM))
      }

      "not identify spectrum as LC/MS or CE/MS" in {
        assert(processedSpectrum.tags.forall(_.text != CommonTags.LCMS_SPECTRUM))
        assert(processedSpectrum.tags.forall(_.text != CommonTags.CEMS_SPECTRUM))
      }
    }
  }
}
