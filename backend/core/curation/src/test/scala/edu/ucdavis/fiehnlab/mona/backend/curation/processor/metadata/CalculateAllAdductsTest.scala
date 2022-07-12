package edu.ucdavis.fiehnlab.mona.backend.curation.processor.metadata

import com.typesafe.scalalogging.LazyLogging
import org.scalatest.matchers.should.Matchers
import org.scalatest.wordspec.AnyWordSpec
import edu.ucdavis.fiehnlab.mona.backend.core.domain.Spectrum
import edu.ucdavis.fiehnlab.mona.backend.core.domain.io.json.JSONDomainReader

import java.io.InputStreamReader

class CalculateAllAdductsTest extends AnyWordSpec with Matchers with LazyLogging{

  val reader: JSONDomainReader[Spectrum] = JSONDomainReader.create[Spectrum]

  "this processor" when {
    val processor = new CalculateAllAdducts

    val exampleRecord: Spectrum = JSONDomainReader.create[Spectrum].read(new InputStreamReader(getClass.getResourceAsStream("/monaRecord.json")))
    "given a spectra" must {
      "correctly calculate adduct list" in {
        val processedSpectrum = processor.process(exampleRecord)
        logger.info(s"${processedSpectrum.compound(1).metaData.filter(x => x.category == "theoretical adduct")}")
        assert(processedSpectrum.compound(1).metaData.filter(x => x.category == "theoretical adduct").length == 50)
      }

      "spot check calculations" in {
        val processedSpectrum = processor.process(exampleRecord)
        val sortedMetaData = processedSpectrum.compound(1).metaData.filter(x => x.category == "theoretical adduct")
        sortedMetaData.find(x => x.name == "[M+3H]+").get.value shouldBe 138.11185251733335
        sortedMetaData.find(x => x.name == "[M+2ACN+2H]+").get.value shouldBe 247.690687776
        sortedMetaData.find(x => x.name == "[M+Na-2H]-").get.value shouldBe 432.28839555200005
      }
    }
  }
}
