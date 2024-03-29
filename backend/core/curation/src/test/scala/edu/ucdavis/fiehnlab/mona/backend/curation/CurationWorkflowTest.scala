package edu.ucdavis.fiehnlab.mona.backend.curation

import java.io.InputStreamReader
import edu.ucdavis.fiehnlab.mona.backend.core.auth.jwt.config.JWTAuthenticationConfig
import edu.ucdavis.fiehnlab.mona.backend.core.domain.Spectrum
import edu.ucdavis.fiehnlab.mona.backend.core.domain.io.json.JSONDomainReader
import edu.ucdavis.fiehnlab.mona.backend.core.persistence.rest.client.config.RestClientTestConfig
import edu.ucdavis.fiehnlab.mona.backend.curation.config.CurationConfig
import org.junit.runner.RunWith
import org.scalatest.matchers.should.Matchers
import org.scalatest.wordspec.AnyWordSpec
import org.springframework.batch.item.ItemProcessor
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.test.context.{ActiveProfiles, TestContextManager}
import org.springframework.test.context.junit4.SpringRunner

/**
  * Created by sajjan on 2/13/19.
  */
@SpringBootTest(classes = Array(classOf[RestClientTestConfig], classOf[CurationConfig], classOf[JWTAuthenticationConfig]))
@ActiveProfiles(Array("test", "mona.persistence", "mona.persistence.init"))
class CurationWorkflowTest extends AnyWordSpec {

  @Autowired
  val curationWorkflow: ItemProcessor[Spectrum, Spectrum] = null

  val reader: JSONDomainReader[Spectrum] = JSONDomainReader.create[Spectrum]

  new TestContextManager(this.getClass).prepareTestInstance(this)

  "the curation workflow" should {
    "curate MSJ00001" in {
      val spectrum = reader.read(new InputStreamReader(getClass.getResourceAsStream("/MSJ00001.json")))
      val processedSpectrum = curationWorkflow.process(spectrum)
      assert(processedSpectrum.getLastCurated != null)
    }

    "curate PT201840" in {
      val spectrum = reader.read(new InputStreamReader(getClass.getResourceAsStream("/PT201840.json")))
      val processedSpectrum = curationWorkflow.process(spectrum)
      assert(processedSpectrum.getLastCurated != null)
    }
  }
}
