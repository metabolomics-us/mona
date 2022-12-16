package edu.ucdavis.fiehnlab.mona.backend.curation.processor.metadata

import com.typesafe.scalalogging.LazyLogging
import edu.ucdavis.fiehnlab.mona.backend.core.domain.Spectrum
import org.scalatest.matchers.should.Matchers
import org.scalatest.wordspec.AnyWordSpec
import edu.ucdavis.fiehnlab.mona.backend.core.domain.io.json.JSONDomainReader

import java.io.InputStreamReader
import scala.jdk.CollectionConverters._

class CalculateAllAdductsTest extends AnyWordSpec with Matchers with LazyLogging{

  val reader: JSONDomainReader[Spectrum] = JSONDomainReader.create[Spectrum]

  "this processor" when {
    val processor = new CalculateAllAdducts

    val exampleRecord: Spectrum = JSONDomainReader.create[Spectrum].read(new InputStreamReader(getClass.getResourceAsStream("/monaRecord2.json")))
    "given a spectra" must {
      "correctly calculate adduct list" in {
        val processedSpectrum = processor.process(exampleRecord)
        logger.info(s"${processedSpectrum.getCompound.get(0).getMetaData.asScala.filter(x => x.getCategory == "theoretical adduct")}")
        assert(processedSpectrum.getCompound.get(0).getMetaData.asScala.filter(x => x.getCategory == "theoretical adduct").length == 17)
      }

      "spot check calculations" in {
        val processedSpectrum = processor.process(exampleRecord)
        val sortedMetaData = processedSpectrum.getCompound.get(0).getMetaData.asScala.filter(x => x.getCategory == "theoretical adduct")
        sortedMetaData.find(x => x.getName == "[M+H]+").get.getValue.toDouble shouldBe 165.041395368 +- 1.0e-4
        sortedMetaData.find(x => x.getName == "[2M+NH4]+").get.getValue.toDouble shouldBe 346.105430736 +- 1.0e-4
        sortedMetaData.find(x => x.getName == "[2M+HAc-H]-").get.getValue.toDouble shouldBe 387.110880736 +- 1.0e-4
      }
    }
  }
}
