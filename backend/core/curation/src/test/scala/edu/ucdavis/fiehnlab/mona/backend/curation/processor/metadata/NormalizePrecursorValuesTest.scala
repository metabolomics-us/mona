package edu.ucdavis.fiehnlab.mona.backend.curation.processor.metadata

import java.io.InputStreamReader

import edu.ucdavis.fiehnlab.mona.backend.core.domain.{MetaData, Spectrum}
import edu.ucdavis.fiehnlab.mona.backend.core.domain.io.json.JSONDomainReader
import edu.ucdavis.fiehnlab.mona.backend.curation.util.CommonMetaData
import org.scalatest.WordSpec

/**
  * Created by sajjan on 2/4/19.
  */
class NormalizePrecursorValuesTest extends WordSpec {

  val reader: JSONDomainReader[Spectrum] = JSONDomainReader.create[Spectrum]

  "this processor" when {

    val processor = new NormalizePrecursorValues

    val spectrum: Spectrum = reader.read(new InputStreamReader(getClass.getResourceAsStream("/monaRecord.json")))
    val metaData = spectrum.metaData

    "given a spectra" must {
      "remove invalid precursor type ''" in {
        val inputSpectrum = spectrum.copy(
          metaData = metaData :+ MetaData("", computed = false, hidden = false, CommonMetaData.PRECURSOR_TYPE, null, "", "", "")
        )
        val processedSpectrum = processor.process(inputSpectrum)

        assert(processedSpectrum.metaData.length == inputSpectrum.metaData.length - 1)
      }

      "remove invalid precursor type 'MS/MS'" in {
        val inputSpectrum = spectrum.copy(
          metaData = metaData :+ MetaData("", computed = false, hidden = false, CommonMetaData.PRECURSOR_TYPE, null, "", "", "MS/MS")
        )
        val processedSpectrum = processor.process(inputSpectrum)

        assert(processedSpectrum.metaData.length == inputSpectrum.metaData.length - 1)
      }

      "not remove valid precursor type" in {
        val inputSpectrum = spectrum.copy(
          metaData = metaData :+ MetaData("", computed = false, hidden = false, CommonMetaData.PRECURSOR_TYPE, null, "", "", "[M+H]+")
        )
        val processedSpectrum = processor.process(inputSpectrum)

        assert(processedSpectrum.metaData.length == inputSpectrum.metaData.length)
      }

      "handle MS^n precursor information" in {
        val inputSpectrum = spectrum.copy(
          metaData = metaData.filter(x => x.name.toLowerCase != CommonMetaData.PRECURSOR_MASS.toLowerCase && x.name.toLowerCase != CommonMetaData.PRECURSOR_TYPE.toLowerCase)
            :+ MetaData("", computed = false, hidden = false, CommonMetaData.PRECURSOR_TYPE, null, "", "", "[M+CH3COO]-/[M-CH3]-")
            :+ MetaData("", computed = false, hidden = false, CommonMetaData.PRECURSOR_MASS, null, "", "", "842.59/768.15")
        )
        val processedSpectrum = processor.process(inputSpectrum)

        assert(processedSpectrum.metaData.length == inputSpectrum.metaData.length + 2)
        assert(processedSpectrum.metaData.exists(x => x.name == "original " + CommonMetaData.PRECURSOR_MASS))
        assert(processedSpectrum.metaData.exists(x => x.name == "original " + CommonMetaData.PRECURSOR_TYPE))
        assert(processedSpectrum.metaData.exists(x => x.name == CommonMetaData.PRECURSOR_MASS))
        assert(processedSpectrum.metaData.exists(x => x.name == CommonMetaData.PRECURSOR_TYPE && x.value == "[M-CH3]-"))
      }
    }
  }
}
