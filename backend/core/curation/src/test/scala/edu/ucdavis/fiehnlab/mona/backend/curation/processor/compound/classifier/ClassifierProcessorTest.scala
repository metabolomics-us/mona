package edu.ucdavis.fiehnlab.mona.backend.curation.processor.compound.classifier

import java.io.InputStreamReader

import edu.ucdavis.fiehnlab.mona.backend.core.auth.jwt.config.JWTAuthenticationConfig
import edu.ucdavis.fiehnlab.mona.backend.core.domain.Spectrum
import edu.ucdavis.fiehnlab.mona.backend.core.domain.io.json.JSONDomainReader
import edu.ucdavis.fiehnlab.mona.backend.core.persistence.rest.client.config.RestClientTestConfig
import edu.ucdavis.fiehnlab.mona.backend.curation.TestConfig
import edu.ucdavis.fiehnlab.mona.backend.curation.processor.RemoveComputedData
import org.junit.runner.RunWith
import org.scalatest.WordSpec
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.{SpringApplicationConfiguration, WebIntegrationTest}
import org.springframework.context.annotation.{Bean, Configuration}
import org.springframework.test.context.TestContextManager
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner
import org.springframework.web.client.RestTemplate

/**
  * Created by wohlgemuth on 5/5/16.
  */
@RunWith(classOf[SpringJUnit4ClassRunner])
@SpringApplicationConfiguration(classes = Array(classOf[RestClientTestConfig], classOf[TestConfig], classOf[JWTAuthenticationConfig]))
@WebIntegrationTest(Array("server.port=44444"))
class ClassifierProcessorTest extends WordSpec {

  val reader = JSONDomainReader.create[Spectrum]

  @Autowired
  val processor:ClassifierProcessor = null

  new TestContextManager(this.getClass()).prepareTestInstance(this)

  "ClassifierProcessorTest" should {

    val input = new InputStreamReader(getClass.getResourceAsStream("/monaRecord.json"))

    val spectrumGiven: Spectrum = reader.read(input)

    "process" in {

      assert(processor != null)
      val output = processor.process(spectrumGiven)

      output.compound.foreach{ compound =>
        assert(compound.classification.length > 0)

      }
    }
  }
}