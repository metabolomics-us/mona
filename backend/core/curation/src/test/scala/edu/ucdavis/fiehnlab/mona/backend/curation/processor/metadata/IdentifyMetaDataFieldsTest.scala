package edu.ucdavis.fiehnlab.mona.backend.curation.processor.metadata

import java.io.InputStreamReader

import edu.ucdavis.fiehnlab.mona.backend.core.domain.{Spectrum, Tags}
import edu.ucdavis.fiehnlab.mona.backend.core.domain.io.json.JSONDomainReader
import org.scalatest.WordSpec

/**
  * Created by sajjan on 4/05/16.
  */
class IdentifyMetaDataFieldsTest extends WordSpec {

  val reader: JSONDomainReader[Spectrum] = JSONDomainReader.create[Spectrum]

  "this processor" when {
    val processor = new IdentifyMetaDataFields

    val exampleRecords: Array[Spectrum] = JSONDomainReader.create[Array[Spectrum]].read(new InputStreamReader(getClass.getResourceAsStream("/monaRecords.json")))

    "given a spectra" must {
      exampleRecords.foreach { spectrum: Spectrum =>
        // Add LC-MS tag since these spectra haven't been curated
        val processedSpectrum = processor.process(spectrum.copy(tags = spectrum.tags :+ Tags(ruleBased = false, "LC-MS")))

        assert(processedSpectrum.score.impacts.exists(_.reason.toLowerCase.contains("instrument information")))
        assert(processedSpectrum.score.impacts.exists(_.reason.toLowerCase.contains("collision energy")))
        assert(processedSpectrum.score.impacts.exists(_.reason.toLowerCase.contains("retention time/index")))
        assert(processedSpectrum.score.impacts.exists(_.reason.toLowerCase.contains("column information")))
        assert(processedSpectrum.score.impacts.exists(_.reason.toLowerCase.contains("precursor type")))
        assert(processedSpectrum.score.impacts.exists(_.reason.toLowerCase.contains("precursor m/z")))
      }
    }
  }
}
