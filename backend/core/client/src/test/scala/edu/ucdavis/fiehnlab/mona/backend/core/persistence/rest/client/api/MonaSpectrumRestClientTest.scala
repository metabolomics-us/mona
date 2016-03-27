package edu.ucdavis.fiehnlab.mona.backend.core.persistence.rest.client.api

import java.io.InputStreamReader

import edu.ucdavis.fiehnlab.mona.backend.core.domain.Spectrum
import edu.ucdavis.fiehnlab.mona.backend.core.domain.io.json.JSONDomainReader
import edu.ucdavis.fiehnlab.mona.backend.core.persistence.rest.client.config.RestClientTestConfig
import org.junit.runner.RunWith
import org.scalatest.{BeforeAndAfter, FunSuite}
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.{SpringApplicationConfiguration, WebIntegrationTest}
import org.springframework.test.context.TestContextManager
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner

/**
  * Created by wohlg_000 on 3/8/2016.
  */
@RunWith(classOf[SpringJUnit4ClassRunner])
@SpringApplicationConfiguration(classes = Array(classOf[RestClientTestConfig]))
@WebIntegrationTest(Array("server.port=44444"))
class MonaSpectrumRestClientTest extends GenericRestClientTest {

  @Autowired
  val monaSpectrumRestClient: MonaSpectrumRestClient = null

  //required for spring and scala tes
  new TestContextManager(this.getClass).prepareTestInstance(this)

  val exampleRecords: Array[Spectrum] = JSONDomainReader.create[Array[Spectrum]].read(new InputStreamReader(getClass.getResourceAsStream("/monaRecords.json")))


  "we must be able to" must {
    "get all available meta data names" in {

      val set = monaSpectrumRestClient.listMetaDataNames

      assert(set.size > 0)
    }

    "get all available meta data values for a given name" in {
      val set: Array[String] = monaSpectrumRestClient.listMetaDataNames

      assert(set.size > 0)
      for (s <- set) {
        val content = monaSpectrumRestClient.listMetaDataValues(s)
        assert(content.size > 0)
      }
    }
  }

}
