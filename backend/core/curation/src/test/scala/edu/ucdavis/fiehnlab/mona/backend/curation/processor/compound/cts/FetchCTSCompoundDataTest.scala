package edu.ucdavis.fiehnlab.mona.backend.curation.processor.compound.cts

import java.io.InputStreamReader

import edu.ucdavis.fiehnlab.mona.backend.core.domain.Spectrum
import edu.ucdavis.fiehnlab.mona.backend.core.domain.io.json.JSONDomainReader
import edu.ucdavis.fiehnlab.mona.backend.core.persistence.rest.client.config.RestClientConfig
import edu.ucdavis.fiehnlab.mona.backend.curation.processor.compound.CompoundTestConfig
import org.junit.runner.RunWith
import org.scalatest.WordSpec
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.test.context.TestContextManager
import org.springframework.test.context.junit4.SpringRunner

/**
  * Created by sajjan on 3/14/16.
  */
@RunWith(classOf[SpringRunner])
@SpringBootTest(classes = Array(classOf[CompoundTestConfig], classOf[RestClientConfig]))
class FetchCTSCompoundDataTest extends WordSpec {

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
          processedSpectrum.compound.foreach { x =>
            assert(x.names.exists(x => x.source == "cts" && x.computed))
          }
        }

        "possess compound properties obtained from the CTS" in {
          processedSpectrum.compound.foreach { x =>
            assert(x.metaData.exists(x => x.category == "compound properties" && x.computed))
          }
        }

        "possess external ids obtained from the CTS" in {
          processedSpectrum.compound.foreach { x =>
            assert(x.metaData.exists(x => x.category == "external ids" && x.computed))
          }
        }
      }
    }
  }
}