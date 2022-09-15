package edu.ucdavis.fiehnlab.mona.backend.curation.processor.metadata

import com.typesafe.scalalogging.LazyLogging

import java.io.InputStreamReader
import edu.ucdavis.fiehnlab.mona.backend.core.domain.dao.{MetaDataDAO, Spectrum}
import edu.ucdavis.fiehnlab.mona.backend.core.domain.io.json.JSONDomainReader
import edu.ucdavis.fiehnlab.mona.backend.curation.util.CommonMetaData
import org.scalatest.wordspec.AnyWordSpec

import scala.jdk.CollectionConverters._

/**
  * Created by sajjan on 2/4/19.
  */
class NormalizePrecursorValuesTest extends AnyWordSpec with LazyLogging{

  val reader: JSONDomainReader[Spectrum] = JSONDomainReader.create[Spectrum]

  "this processor" when {

    val processor = new NormalizePrecursorValues

    val spectrum: Spectrum = reader.read(new InputStreamReader(getClass.getResourceAsStream("/monaRecord.json")))
    val metaData = spectrum.getMetaData

    "given a spectra" must {
      "remove invalid precursor type ''" in {
        val inputSpectrum = new Spectrum(spectrum)
        inputSpectrum.setMetaData((metaData.asScala :+ new MetaDataDAO("", CommonMetaData.PRECURSOR_TYPE, "", false, "", false, "")).asJava)
        val copySpectrum = new Spectrum(inputSpectrum)
        val processedSpectrum = processor.process(inputSpectrum)
        assert(processedSpectrum.getMetaData.size() == (copySpectrum.getMetaData.size() - 1))
      }

     "remove invalid precursor type 'MS/MS'" in {
        val inputSpectrum = new Spectrum(spectrum)
        inputSpectrum.setMetaData((metaData.asScala :+ new MetaDataDAO("", CommonMetaData.PRECURSOR_TYPE, "MS/MS", false, "", false, "")).asJava)
        val copySpectrum = new Spectrum(inputSpectrum)
        val processedSpectrum = processor.process(inputSpectrum)

        assert(processedSpectrum.getMetaData.size() == copySpectrum.getMetaData.size() - 1)
      }

      "not remove valid precursor type" in {
        val inputSpectrum = new Spectrum(spectrum)
        inputSpectrum.setMetaData((metaData.asScala :+ new MetaDataDAO("", CommonMetaData.PRECURSOR_TYPE, "[M+H]+", false, "", false, "")).asJava)
        val copySpectrum = new Spectrum(inputSpectrum)
        val processedSpectrum = processor.process(inputSpectrum)

        assert(processedSpectrum.getMetaData.size() == copySpectrum.getMetaData.size())
      }

      "handle MS^n precursor information" in {
        val inputSpectrum = new Spectrum(spectrum)
        inputSpectrum.setMetaData((metaData.asScala.filter(x => x.getName.toLowerCase != CommonMetaData.PRECURSOR_MASS.toLowerCase && x.getName.toLowerCase != CommonMetaData.PRECURSOR_TYPE.toLowerCase)
        :+ new MetaDataDAO("", CommonMetaData.PRECURSOR_TYPE, "[M+CH3COO]-/[M-CH3]-", false, "", false, "")
        :+ new MetaDataDAO("", CommonMetaData.PRECURSOR_MASS, "842.59/768.15", false, "", false, "")).asJava
        )
        val copySpectrum = new Spectrum(inputSpectrum)

        val processedSpectrum = processor.process(inputSpectrum)

        assert(processedSpectrum.getMetaData.size() == copySpectrum.getMetaData.size() + 2)
        assert(processedSpectrum.getMetaData.asScala.exists(x => x.getName == "original " + CommonMetaData.PRECURSOR_MASS))
        assert(processedSpectrum.getMetaData.asScala.exists(x => x.getName == "original " + CommonMetaData.PRECURSOR_TYPE))
        assert(processedSpectrum.getMetaData.asScala.exists(x => x.getName == CommonMetaData.PRECURSOR_MASS))
        assert(processedSpectrum.getMetaData.asScala.exists(x => x.getName == CommonMetaData.PRECURSOR_TYPE && x.getValue == "[M-CH3]-"))
      }
    }
  }
}
