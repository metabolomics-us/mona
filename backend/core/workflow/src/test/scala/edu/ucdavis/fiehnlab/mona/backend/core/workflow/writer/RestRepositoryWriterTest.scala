package edu.ucdavis.fiehnlab.mona.backend.core.workflow.writer

import java.io.InputStreamReader

import edu.ucdavis.fiehnlab.mona.backend.core.domain.Types.Spectrum
import edu.ucdavis.fiehnlab.mona.backend.core.domain.io.json.JSONDomainReader
import edu.ucdavis.fiehnlab.mona.backend.core.persistence.rest.client.api.GenericRestClient
import edu.ucdavis.fiehnlab.mona.backend.core.persistence.rest.client.config.RestClientTestConfig
import edu.ucdavis.fiehnlab.mona.backend.core.workflow.config.WorkflowConfiguration
import edu.ucdavis.fiehnlab.mona.backend.core.workflow.reader.RestRepositoryReader
import org.junit.runner.RunWith
import org.scalatest.{WordSpec, FunSuite}
import org.springframework.beans.factory.annotation.{Autowired, Value}
import org.springframework.boot.test.{WebIntegrationTest, SpringApplicationConfiguration}
import org.springframework.test.context.TestContextManager
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner
import scala.collection.JavaConverters._
/**
  * Created by wohlg on 3/11/2016.
  */
@RunWith(classOf[SpringJUnit4ClassRunner])
@SpringApplicationConfiguration(classes = Array(classOf[RestClientTestConfig], classOf[WorkflowConfiguration]))
@WebIntegrationTest(Array("server.port=44444"))
class RestRepositoryWriterTest extends WordSpec {

  @Value( """${local.server.port}""")
  val port: Int = 0

  @Autowired
  val spectrumRestClient: GenericRestClient[Spectrum, String] = null

  @Autowired
  val writer: RestRepositoryWriter = null
  new TestContextManager(this.getClass()).prepareTestInstance(this)

  "a writer " when {
    val exampleRecords: Array[Spectrum] = JSONDomainReader.create[Array[Spectrum]].read(new InputStreamReader(getClass.getResourceAsStream("/monaRecords.json")))

    "given a list of spectra" should {
      "upload them to the server" in {

        writer.write(exampleRecords.toList.asJava)

        assert(spectrumRestClient.count() == exampleRecords.length)
      }
    }
  }
}
