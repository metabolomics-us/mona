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

    val exampleRecord: Spectrum = JSONDomainReader.create[Spectrum].read(new InputStreamReader(getClass.getResourceAsStream("/monaRecord2.json")))
    "given a spectra" must {
      "correctly calculate adduct list" in {
        val processedSpectrum = processor.process(exampleRecord)
        logger.info(s"${processedSpectrum.compound(0).metaData.filter(x => x.category == "theoretical adduct")}")
        assert(processedSpectrum.compound(0).metaData.filter(x => x.category == "theoretical adduct").length == 16)
      }

      "spot check calculations" in {
        val processedSpectrum = processor.process(exampleRecord)
        val sortedMetaData = processedSpectrum.compound(0).metaData.filter(x => x.category == "theoretical adduct")
        sortedMetaData.find(x => x.name == "[M+H]+").get.value shouldBe 165.041395368
        sortedMetaData.find(x => x.name == "[2M+NH4]+").get.value shouldBe 346.105430736
        sortedMetaData.find(x => x.name == "[2M+HAc-H]-").get.value shouldBe 387.110880736
      }
    }
  }
}
