package edu.ucdavis.fiehnlab.mona.backend.curation.reader

import java.io.InputStreamReader

import edu.ucdavis.fiehnlab.mona.backend.core.domain.Spectrum
import edu.ucdavis.fiehnlab.mona.backend.core.domain.io.json.JSONDomainReader
import edu.ucdavis.fiehnlab.mona.backend.core.persistence.rest.client.api.GenericRestClient
import edu.ucdavis.fiehnlab.mona.backend.core.persistence.rest.client.config.RestClientTestConfig
import edu.ucdavis.fiehnlab.mona.backend.curation.TestConfig
import org.junit.runner.RunWith
import org.scalatest.WordSpec
import org.springframework.beans.factory.annotation.{Autowired, Value}
import org.springframework.boot.test.{SpringApplicationConfiguration, WebIntegrationTest}
import org.springframework.test.context.TestContextManager
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner

/**
  * Created by wohlgemuth on 3/18/16.
  */
@RunWith(classOf[SpringJUnit4ClassRunner])
@SpringApplicationConfiguration(classes = Array(classOf[RestClientTestConfig], classOf[TestConfig]))
@WebIntegrationTest(Array("server.port=44444"))
class RestRepositoryReaderTest extends WordSpec {
  @Value( """${local.server.port}""")
  val port: Int = 0

  @Autowired
  val spectrumRestClient: GenericRestClient[Spectrum, String] = null

  @Autowired
  val restRepositoryReaderAll: RestRepositoryReader = null

  @Autowired
  val restRepositoryReaderWithQuery: RestRepositoryReader = null


  //required for spring and scala tes
  new TestContextManager(this.getClass()).prepareTestInstance(this)

  "RestRepositoryReaderTest" should {

    val exampleRecords: Array[Spectrum] = JSONDomainReader.create[Array[Spectrum]].read(new InputStreamReader(getClass.getResourceAsStream("/monaRecords.json")))

    spectrumRestClient.list().foreach(x =>
      spectrumRestClient.delete(x.id)
    )
    for (spec <- exampleRecords) {
      spectrumRestClient.add(spec)
    }


    "read" in {

      var count = 0

      var data: Spectrum = null

      do {
        data = restRepositoryReaderAll.read()
        if (data != null) {
          count = count + 1
        }
      }
      while (data != null)

      assert(count == exampleRecords.length)
    }

    "read with a specific query" in {

      var count = 0

      var data: Spectrum = null

      do {
        data = restRepositoryReaderWithQuery.read()
        if (data != null) {
          count = count + 1
        }

      }
      while (data != null)

      assert(count == 25)
    }

  }
}
