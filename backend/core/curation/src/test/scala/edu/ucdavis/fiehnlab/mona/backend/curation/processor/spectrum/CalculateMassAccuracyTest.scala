package edu.ucdavis.fiehnlab.mona.backend.curation.processor.spectrum

import edu.ucdavis.fiehnlab.mona.backend.core.domain.{MetaData, Spectrum}
import java.io.InputStreamReader

import edu.ucdavis.fiehnlab.mona.backend.core.domain.io.json.JSONDomainReader
import edu.ucdavis.fiehnlab.mona.backend.curation.util.CommonMetaData
import org.scalatest.wordspec.AnyWordSpec
import org.scalatest.matchers.should.Matchers
import scala.jdk.CollectionConverters._


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
        exampleRecord.setMetaData(exampleRecord.getMetaData.asScala.filter(!_.getComputed).asJava)
        val processedSpectrum = processor.process(exampleRecord)

        val massAccuracy: Option[MetaData] = processedSpectrum.getMetaData.asScala.find(_.getName == CommonMetaData.MASS_ACCURACY)
        massAccuracy.isDefined shouldBe true
        massAccuracy.get.getValue.toDouble shouldBe 52.63049236000388 +- 1.0e-4

        val massError: Option[MetaData] = processedSpectrum.getMetaData.asScala.find(_.getName == CommonMetaData.MASS_ERROR)
        massError.isDefined shouldBe true
        massError.get.getValue.toDouble shouldBe -0.021 +- 1.0e-2
      }
    }
  }
}
