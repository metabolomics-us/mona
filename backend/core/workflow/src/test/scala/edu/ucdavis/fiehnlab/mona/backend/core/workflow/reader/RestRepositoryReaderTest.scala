package edu.ucdavis.fiehnlab.mona.backend.core.workflow.reader

import java.io.InputStreamReader

import edu.ucdavis.fiehnlab.mona.backend.core.domain.Types.Spectrum
import edu.ucdavis.fiehnlab.mona.backend.core.domain.io.json.JSONDomainReader
import edu.ucdavis.fiehnlab.mona.backend.core.persistence.rest.client.api.GenericRestClient
import edu.ucdavis.fiehnlab.mona.backend.core.workflow.config.WorkflowConfiguration
import org.junit.runner.RunWith
import org.scalatest.WordSpec
import org.springframework.beans.factory.annotation.{Autowired, Value}
import org.springframework.boot.test.{SpringApplicationConfiguration, WebIntegrationTest}
import org.springframework.context.annotation.{Configuration, Bean}
import org.springframework.test.context.TestContextManager
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner
import edu.ucdavis.fiehnlab.mona.backend.core.persistence.rest.client.config.{RestClientTestConfig}

@Configuration
class ReaderConfig {
  @Bean
  def restRepositoryReaderAll = new RestRepositoryReader()

  @Bean
  def restRepositoryReaderWithQuery = new RestRepositoryReader("""metaData=q='name=="ion mode" and value==negative'""")


}

/**
  * Created by wohlgemuth on 3/18/16.
  */
@RunWith(classOf[SpringJUnit4ClassRunner])
@SpringApplicationConfiguration(classes = Array(classOf[RestClientTestConfig], classOf[WorkflowConfiguration], classOf[ReaderConfig]))
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


    "we should be able to delete all data" in {
      spectrumRestClient.list().foreach(x =>
        spectrumRestClient.delete(x.id)
      )
    }


    "we should be able to add spectra" in {
      for (spec <- exampleRecords) {
        spectrumRestClient.add(spec)
      }
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
