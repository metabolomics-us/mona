package edu.ucdavis.fiehnlab.mona.backend.curation.processor.metadata

import java.io.InputStreamReader

import edu.ucdavis.fiehnlab.mona.backend.core.domain.Spectrum
import edu.ucdavis.fiehnlab.mona.backend.core.domain.io.json.JSONDomainReader
import edu.ucdavis.fiehnlab.mona.backend.curation.util.CommonMetaData
import org.scalatest.WordSpec

/**
  * Created by sajjan on 4/05/16.
  */
class NormalizeMSLevelValueTest extends WordSpec {

  val reader: JSONDomainReader[Spectrum] = JSONDomainReader.create[Spectrum]

  "this processor" when {

    val processor = new NormalizeMSLevelValue

    val input = new InputStreamReader(getClass.getResourceAsStream("/monaRecord.json"))
    val spectrumGiven: Spectrum = reader.read(input)

    val exampleRecords: Array[Spectrum] = JSONDomainReader.create[Array[Spectrum]].read(new InputStreamReader(getClass.getResourceAsStream("/monaRecords.json")))

    "given a spectra" must {
      exampleRecords.foreach { spectrum: Spectrum =>
        val processedSpectrum = processor.process(spectrum)

        assert(processedSpectrum.metaData.exists(_.name == CommonMetaData.MS_LEVEL))
        assert(processedSpectrum.score.impacts.exists(_.reason.contains("MS type/level")))
      }
    }
  }
}
