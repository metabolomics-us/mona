package edu.ucdavis.fiehnlab.mona.backend.curation.processor.metadata

import java.io.InputStreamReader

import edu.ucdavis.fiehnlab.mona.backend.core.domain.io.json.JSONDomainReader
import edu.ucdavis.fiehnlab.mona.backend.core.domain.{MetaData, Spectrum}
import edu.ucdavis.fiehnlab.mona.backend.curation.util.CommonMetaData
import org.scalatest.wordspec.AnyWordSpec
import org.scalatest.matchers.should.Matchers

class SpectralEntropyTest extends AnyWordSpec with Matchers {
  val reader: JSONDomainReader[Spectrum] = JSONDomainReader.create[Spectrum]

  "this processor" when {
    val processor = new SpectralEntropy
    val exampleRecord: Spectrum = JSONDomainReader.create[Spectrum].read(new InputStreamReader(getClass.getResourceAsStream("/monaRecord.json")))
    var newRecord: Spectrum = Spectrum(null, exampleRecord.id, null, null, null, null, null, null, "80:30 100.001:10 100.002:10 200:80 300:20", null, null, null, null, null)
    "given a spectra" must {
      "correctly calculate the spectrum entropy" in {
        val processedSpectrum = processor.process(newRecord.copy(metaData = exampleRecord.metaData.filter(!_.computed)))

        val spectral_entropy: Option[MetaData] = processedSpectrum.metaData.find(_.name == CommonMetaData.SPECTRAL_ENTROPY)
        spectral_entropy.isDefined shouldBe true
        spectral_entropy.get.value.toString.toDouble shouldBe 1.1944 +- 1.0e-4

        val normalized_entropy: Option[MetaData] = processedSpectrum.metaData.find(_.name == CommonMetaData.NORMALIZED_ENTROPY)
        normalized_entropy.isDefined shouldBe true
        normalized_entropy.get.value.toString.toDouble shouldBe 0.8616 +- 1.0e-4
      }
    }
  }
}
