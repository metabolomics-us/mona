package edu.ucdavis.fiehnlab.mona.backend.curation.processor.compound.adduct

import java.io.InputStreamReader

import edu.ucdavis.fiehnlab.mona.backend.core.domain.dao.Spectrum
import edu.ucdavis.fiehnlab.mona.backend.core.domain.io.json.JSONDomainReader
import org.scalatest.wordspec.AnyWordSpec
import org.scalatest.matchers.should.Matchers
import scala.jdk.CollectionConverters._

/**
  * Created by sajjan on 3/21/16.
  */
class AdductPredictionTest extends AnyWordSpec with Matchers {

  val reader: JSONDomainReader[Spectrum] = JSONDomainReader.create[Spectrum]

  "this processor" when {
    val processor = new AdductPrediction

    val exampleRecord: Spectrum = JSONDomainReader.create[Spectrum].read(new InputStreamReader(getClass.getResourceAsStream("/monaRecord.json")))

    "given a spectra" must {
      "correctly calculate the precursor type" in {
        val modifiedRecord: Spectrum = new Spectrum(exampleRecord)
        modifiedRecord.setMetaData(exampleRecord.getMetaData.asScala.filter(x => !x.getComputed && x.getName != "precursor type").asJava)
         /* exampleRecord.copy(
          metaData = exampleRecord.metaData.filter(x => !x.computed && x.name != "precursor type")
        )*/
        val processedSpectrum = processor.process(modifiedRecord)

        assert(processedSpectrum.getMetaData.asScala.exists(x => x.getName == "precursor type" && x.getValue == "[M+H]+" && x.getComputed))
      }

      "correctly calculate the precursor m/z" in {
        val modifiedRecord: Spectrum = new Spectrum(exampleRecord)
        modifiedRecord.setMetaData(exampleRecord.getMetaData.asScala.filter(x => !x.getComputed && x.getName != "precursor m/z").asJava)
        /*  exampleRecord.copy(
          metaData = exampleRecord.metaData.filter(x => !x.computed && x.name != "precursor m/z")
        )*/
        val processedSpectrum = processor.process(modifiedRecord)

        assert(processedSpectrum.getMetaData.asScala.exists(x => x.getName == "precursor m/z" && x.getComputed))
      }
    }
  }
}
