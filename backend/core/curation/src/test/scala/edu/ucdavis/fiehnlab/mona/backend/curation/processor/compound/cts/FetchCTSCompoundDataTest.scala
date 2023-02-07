package edu.ucdavis.fiehnlab.mona.backend.curation.processor.compound.cts

import edu.ucdavis.fiehnlab.mona.backend.core.domain.Spectrum
import java.io.InputStreamReader
import edu.ucdavis.fiehnlab.mona.backend.core.domain.io.json.JSONDomainReader
import edu.ucdavis.fiehnlab.mona.backend.core.persistence.rest.client.config.RestClientConfig
import edu.ucdavis.fiehnlab.mona.backend.curation.processor.compound.CompoundTestApplication
import org.junit.runner.RunWith
import org.scalatest.wordspec.AnyWordSpec
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.test.context.{ActiveProfiles, TestContextManager}
import org.springframework.test.context.junit4.SpringRunner

import scala.jdk.CollectionConverters._

/**
  * Created by sajjan on 3/14/16.
  */
@SpringBootTest(classes = Array(classOf[CompoundTestApplication], classOf[RestClientConfig]))
@ActiveProfiles(Array("test", "mona.persistence", "mona.persistence.init"))
class FetchCTSCompoundDataTest extends AnyWordSpec {

  val reader: JSONDomainReader[Spectrum] = JSONDomainReader.create[Spectrum]

  @Autowired
  val ctsProcessor: FetchCTSCompoundData = null

  new TestContextManager(this.getClass).prepareTestInstance(this)

  "this processor" when {
    val input = new InputStreamReader(getClass.getResourceAsStream("/monaRecord.json"))
    val spectrum: Spectrum = reader.read(input)

    "given a spectra" must {
      assert(ctsProcessor != null)

      if (ctsProcessor.ENABLED) {
        val processedSpectrum: Spectrum = ctsProcessor.process(spectrum)

        "possess names obtained from the CTS" in {
          processedSpectrum.getCompound.asScala.foreach { x =>
            assert(x.getNames.asScala.exists(x => x.getSource == "cts" && x.getComputed))
          }
        }

        "possess compound properties obtained from the CTS" in {
          processedSpectrum.getCompound.asScala.foreach { x =>
            assert(x.getMetaData.asScala.exists(x => x.getCategory == "compound properties" && x.getComputed))
          }
        }

        "possess external ids obtained from the CTS" in {
          processedSpectrum.getCompound.asScala.foreach { x =>
            assert(x.getMetaData.asScala.exists(x => x.getCategory == "external ids" && x.getComputed))
          }
        }
      }
    }
  }
}
