package edu.ucdavis.fiehnlab.mona.backend.curation

import java.io.InputStreamReader

import edu.ucdavis.fiehnlab.mona.backend.core.auth.jwt.config.JWTAuthenticationConfig
import edu.ucdavis.fiehnlab.mona.backend.core.domain.Spectrum
import edu.ucdavis.fiehnlab.mona.backend.core.domain.io.json.JSONDomainReader
import edu.ucdavis.fiehnlab.mona.backend.core.persistence.rest.client.config.RestClientTestConfig
import edu.ucdavis.fiehnlab.mona.backend.curation.config.CurationConfig
import org.junit.runner.RunWith
import org.scalatest.WordSpec
import org.springframework.batch.item.ItemProcessor
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.boot.test.context.SpringBootTest.WebEnvironment
import org.springframework.test.context.TestContextManager
import org.springframework.test.context.junit4.SpringRunner

/**
  * Created by sajjan on 2/13/19.
  */
@RunWith(classOf[SpringRunner])
@SpringBootTest(classes = Array(classOf[RestClientTestConfig], classOf[CurationConfig], classOf[JWTAuthenticationConfig]), webEnvironment = WebEnvironment.DEFINED_PORT)
class CurationWorkflowTest extends WordSpec {

  @Autowired
  val curationWorkflow: ItemProcessor[Spectrum, Spectrum] = null

  val reader: JSONDomainReader[Spectrum] = JSONDomainReader.create[Spectrum]

  new TestContextManager(this.getClass).prepareTestInstance(this)

  "the curation workflow" should {
    "curate MSJ00001" in {
      val spectrum = reader.read(new InputStreamReader(getClass.getResourceAsStream("/MSJ00001.json")))
      val processedSpectrum = curationWorkflow.process(spectrum)

    }
  }
}
