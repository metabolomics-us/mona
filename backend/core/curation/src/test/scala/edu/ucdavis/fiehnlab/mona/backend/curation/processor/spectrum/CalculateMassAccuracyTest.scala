package edu.ucdavis.fiehnlab.mona.backend.curation.processor.spectrum

import java.io.InputStreamReader

import edu.ucdavis.fiehnlab.mona.backend.core.domain.io.json.JSONDomainReader
import edu.ucdavis.fiehnlab.mona.backend.core.domain.{MetaData, Spectrum}
import edu.ucdavis.fiehnlab.mona.backend.curation.util.CommonMetaData
import org.scalatest.wordspec.AnyWordSpec
import org.scalatest.matchers.should.Matchers


/**
  * Created by sajjan on 3/21/16.
  */
class CalculateMassAccuracyTest extends AnyWordSpec with Matchers {

  val reader: JSONDomainReader[Spectrum] = JSONDomainReader.create[Spectrum]

  "this processor" when {
    val processor = new CalculateMassAccuracy

    val exampleRecord: Spectrum = JSONDomainReader.create[Spectrum].read(new InputStreamReader(getClass.getResourceAsStream("/monaRecord.json")))

    "given a spectra" must {
      "correctly calculate the mass accuracy" in {
        val processedSpectrum = processor.process(exampleRecord.copy(metaData = exampleRecord.metaData.filter(!_.computed)))

        val massAccuracy: Option[MetaData] = processedSpectrum.metaData.find(_.name == CommonMetaData.MASS_ACCURACY)
        massAccuracy.isDefined shouldBe true
        massAccuracy.get.value.toString.toDouble shouldBe 50.9472 +- 1.0e-4

        val massError: Option[MetaData] = processedSpectrum.metaData.find(_.name == CommonMetaData.MASS_ERROR)
        massError.isDefined shouldBe true
        massError.get.value.toString.toDouble shouldBe -0.021 +- 1.0e-4
      }
    }
  }
}
