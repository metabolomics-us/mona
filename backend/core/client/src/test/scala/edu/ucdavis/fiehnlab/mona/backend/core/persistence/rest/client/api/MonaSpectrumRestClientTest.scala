package edu.ucdavis.fiehnlab.mona.backend.core.persistence.rest.client.api

import java.io.InputStreamReader

import edu.ucdavis.fiehnlab.mona.backend.core.auth.jwt.config.JWTAuthenticationConfig
import edu.ucdavis.fiehnlab.mona.backend.core.domain.Spectrum
import edu.ucdavis.fiehnlab.mona.backend.core.domain.io.json.JSONDomainReader
import edu.ucdavis.fiehnlab.mona.backend.core.persistence.rest.client.config.RestClientTestConfig
import org.junit.runner.RunWith
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.boot.test.context.SpringBootTest.WebEnvironment
import org.springframework.test.context.TestContextManager
import org.springframework.test.context.junit4.SpringRunner

/**
  * Created by wohlg_000 on 3/8/2016.
  */
@RunWith(classOf[SpringRunner])
@SpringBootTest(classes = Array(classOf[RestClientTestConfig], classOf[JWTAuthenticationConfig]), webEnvironment = WebEnvironment.DEFINED_PORT, properties = Array("server.port=44444"))
class MonaSpectrumRestClientTest extends AbstractRestClientTest {

  @Autowired
  val monaSpectrumRestClient: MonaSpectrumRestClient = null

  //required for spring and scala tes
  new TestContextManager(this.getClass).prepareTestInstance(this)

  val exampleRecords: Array[Spectrum] = JSONDomainReader.create[Array[Spectrum]].read(new InputStreamReader(getClass.getResourceAsStream("/monaRecords.json")))


  "we must be able to" must {
    "get all available meta data names" in {
      val set = monaSpectrumRestClient.listMetaDataNames
      assert(set.nonEmpty)
    }

    "get all available meta data values for a given name" in {
      val set: Array[String] = monaSpectrumRestClient.listMetaDataNames
      assert(set.nonEmpty)

      for (s <- set) {
        val content = monaSpectrumRestClient.listMetaDataValues(s)
        assert(content.nonEmpty)
      }
    }
  }
}
