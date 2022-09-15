package edu.ucdavis.fiehnlab.mona.backend.curation.processor.metadata

import java.io.InputStreamReader

import edu.ucdavis.fiehnlab.mona.backend.core.domain.io.json.JSONDomainReader
import edu.ucdavis.fiehnlab.mona.backend.core.domain.dao.{Spectrum, TagDAO}
import org.scalatest.wordspec.AnyWordSpec
import scala.jdk.CollectionConverters._
/**
  * Created by sajjan on 4/05/16.
  */
class IdentifyMetaDataFieldsTest extends AnyWordSpec {

  val reader: JSONDomainReader[Spectrum] = JSONDomainReader.create[Spectrum]

  "this processor" when {
    val processor = new IdentifyMetaDataFields

    val exampleRecords: Array[Spectrum] = JSONDomainReader.create[Array[Spectrum]].read(new InputStreamReader(getClass.getResourceAsStream("/monaRecords.json")))

    "given a spectra" must {
      "score the presence of metadata" in {
        exampleRecords.foreach { spectrum: Spectrum =>
          // Add LC-MS tag since these spectra haven't been curated
          spectrum.setTags((spectrum.getTags.asScala :+ new TagDAO("LC-MS", false)).asJava)
          val processedSpectrum = processor.process(spectrum)

          assert(processedSpectrum.getScore.getImpacts.asScala.exists(_.getReason.toLowerCase.contains("instrument information")))
          assert(processedSpectrum.getScore.getImpacts.asScala.exists(_.getReason.toLowerCase.contains("collision energy")))
          assert(processedSpectrum.getScore.getImpacts.asScala.exists(_.getReason.toLowerCase.contains("retention time/index")))
          assert(processedSpectrum.getScore.getImpacts.asScala.exists(_.getReason.toLowerCase.contains("column information")))
          assert(processedSpectrum.getScore.getImpacts.asScala.exists(_.getReason.toLowerCase.contains("precursor type")))
          assert(processedSpectrum.getScore.getImpacts.asScala.exists(_.getReason.toLowerCase.contains("precursor m/z")))
        }
      }
    }
  }
}
