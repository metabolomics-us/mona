package edu.ucdavis.fiehnlab.mona.backend.curation.processor.compound

import java.io.InputStreamReader

import edu.ucdavis.fiehnlab.mona.backend.core.auth.jwt.config.JWTAuthenticationConfig
import edu.ucdavis.fiehnlab.mona.backend.core.domain.Spectrum
import edu.ucdavis.fiehnlab.mona.backend.core.domain.io.json.JSONDomainReader
import edu.ucdavis.fiehnlab.mona.backend.core.persistence.rest.client.config.RestClientTestConfig
import edu.ucdavis.fiehnlab.mona.backend.curation.TestConfig
import org.junit.runner.RunWith
import org.scalatest.WordSpec
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.{SpringApplicationConfiguration, WebIntegrationTest}
import org.springframework.test.context.TestContextManager
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner

/**
  * Created by sajjan on 9/26/16.
  */
@RunWith(classOf[SpringJUnit4ClassRunner])
@SpringApplicationConfiguration(classes = Array(classOf[RestClientTestConfig], classOf[TestConfig], classOf[JWTAuthenticationConfig]))
@WebIntegrationTest(Array("server.port=44444"))
class CalculateCompoundPropertiesTest extends WordSpec {
  val reader = JSONDomainReader.create[Spectrum]

  @Autowired
  val calculateCompoundProperties: CalculateCompoundProperties = null

  new TestContextManager(this.getClass).prepareTestInstance(this)


  "CalculateCompoundPropertiesTest" should {

    val input = new InputStreamReader(getClass.getResourceAsStream("/monaRecord.json"))
    val spectrum: Spectrum = reader.read(input)

    "process" in {
      assert(calculateCompoundProperties != null)
      val output = calculateCompoundProperties.process(spectrum)

      output.compound.foreach { compound =>
        assert(compound.molFile != null)
        assert(compound.metaData.exists(_.computed))
      }
    }
  }
}