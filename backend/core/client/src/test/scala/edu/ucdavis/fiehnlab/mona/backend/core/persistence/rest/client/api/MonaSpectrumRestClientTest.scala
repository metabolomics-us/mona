package edu.ucdavis.fiehnlab.mona.backend.core.persistence.rest.client.api

import edu.ucdavis.fiehnlab.mona.backend.core.auth.jwt.config.JWTAuthenticationConfig
import edu.ucdavis.fiehnlab.mona.backend.core.persistence.rest.client.config.RestClientTestConfig
import org.junit.runner.RunWith
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.boot.test.context.SpringBootTest.WebEnvironment
import org.springframework.test.context.TestContextManager
import org.springframework.test.context.junit4.SpringRunner

import scala.concurrent.duration._
import scala.language.postfixOps

/**
  * Created by wohlg_000 on 3/8/2016.
  */
@RunWith(classOf[SpringRunner])
@SpringBootTest(classes = Array(classOf[RestClientTestConfig], classOf[JWTAuthenticationConfig]), webEnvironment = WebEnvironment.DEFINED_PORT)
class MonaSpectrumRestClientTest extends AbstractRestClientTest {

  @Autowired
  val monaSpectrumRestClient: MonaSpectrumRestClient = null

  new TestContextManager(this.getClass).prepareTestInstance(this)

  "we must be able to" must {

    "generate statistics" in {
      monaSpectrumRestClient.regenerateStatistics()
    }

    "get all available meta data names" in {
      eventually(timeout(10 seconds)) {
        val set = monaSpectrumRestClient.listMetaDataNames
        assert(set.nonEmpty)
      }
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
