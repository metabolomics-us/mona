package edu.ucdavis.fiehnlab.mona.backend.curation.processor.instrument

import com.typesafe.scalalogging.LazyLogging
import edu.ucdavis.fiehnlab.mona.backend.core.domain.Spectrum

import java.io.InputStreamReader
import edu.ucdavis.fiehnlab.mona.backend.core.domain.io.json.JSONDomainReader
import edu.ucdavis.fiehnlab.mona.backend.curation.util.CommonTags
import org.scalatest.wordspec.AnyWordSpec

import scala.jdk.CollectionConverters._

/**
  * Created by sajjan on 3/21/16.
  */
class IdentifyChromotographyTest extends AnyWordSpec with LazyLogging{

  val reader: JSONDomainReader[Spectrum] = JSONDomainReader.create[Spectrum]

  "this processor" when {
    val processor = new IdentifyChromatography

    "given an LC/MS spectrum" must {
      val lcmsRecord = new InputStreamReader(getClass.getResourceAsStream("/monaRecord.json"))
      val spectrum: Spectrum = reader.read(lcmsRecord)
      val processedSpectrum = processor.process(spectrum)

      "identify spectrum as LC/MS" in {
        assert(processedSpectrum.getTags.asScala.exists(_.getText == CommonTags.LCMS_SPECTRUM))
      }

      "not identify spectrum as GC/MS or CE/MS" in {
        assert(processedSpectrum.getTags.asScala.forall(_.getText != CommonTags.GCMS_SPECTRUM))
        assert(processedSpectrum.getTags.asScala.forall(_.getText != CommonTags.CEMS_SPECTRUM))
      }
    }

    "given an GC/MS spectrum" must {
      val gcmsRecord = new InputStreamReader(getClass.getResourceAsStream("/gcmsRecord.json"))
      val spectrum: Spectrum = reader.read(gcmsRecord)
      val processedSpectrum = processor.process(spectrum)

      "identify spectrum as GC/MS" in {
        assert(processedSpectrum.getTags.asScala.exists(_.getText == CommonTags.GCMS_SPECTRUM))
      }

      "not identify spectrum as LC/MS or CE/MS" in {
        assert(processedSpectrum.getTags.asScala.forall(_.getText != CommonTags.LCMS_SPECTRUM))
        assert(processedSpectrum.getTags.asScala.forall(_.getText != CommonTags.CEMS_SPECTRUM))
      }
    }
  }
}
