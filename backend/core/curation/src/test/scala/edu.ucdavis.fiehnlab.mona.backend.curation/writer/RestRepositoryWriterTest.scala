package edu.ucdavis.fiehnlab.mona.backend.curation.writer

import java.io.InputStreamReader

import edu.ucdavis.fiehnlab.mona.backend.core.auth.jwt.config.JWTAuthenticationConfig
import edu.ucdavis.fiehnlab.mona.backend.core.auth.jwt.repository.UserRepository
import edu.ucdavis.fiehnlab.mona.backend.core.auth.types.{Role, User}
import edu.ucdavis.fiehnlab.mona.backend.core.domain.Spectrum
import edu.ucdavis.fiehnlab.mona.backend.core.domain.io.json.JSONDomainReader
import edu.ucdavis.fiehnlab.mona.backend.core.persistence.rest.client.api.{GenericRestClient, MonaSpectrumRestClient}
import edu.ucdavis.fiehnlab.mona.backend.core.persistence.rest.client.config.RestClientTestConfig
import edu.ucdavis.fiehnlab.mona.backend.curation.TestConfig
import org.junit.runner.RunWith
import org.scalatest.WordSpec
import org.springframework.beans.factory.annotation.{Autowired, Value}
import org.springframework.boot.test.{SpringApplicationConfiguration, WebIntegrationTest}
import org.springframework.test.context.TestContextManager
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner

import scala.collection.JavaConverters._
/**
  * Created by wohlg on 3/11/2016.
  */
@RunWith(classOf[SpringJUnit4ClassRunner])
@SpringApplicationConfiguration(classes = Array(classOf[RestClientTestConfig],classOf[TestConfig],classOf[JWTAuthenticationConfig]))
@WebIntegrationTest(Array("server.port=44444"))
class RestRepositoryWriterTest extends WordSpec {

  @Value( """${local.server.port}""")
  val port: Int = 0

  @Autowired
  val monaSpectrumRestClient: MonaSpectrumRestClient = null


  @Autowired
  val userRepository: UserRepository = null

  @Autowired
  val writer: RestRepositoryWriter = null
  new TestContextManager(this.getClass()).prepareTestInstance(this)

  "a writer " when {
    val exampleRecords: Array[Spectrum] = JSONDomainReader.create[Array[Spectrum]].read(new InputStreamReader(getClass.getResourceAsStream("/monaRecords.json")))

    "given a list of spectra" should {



      "we need to login " in {
        userRepository.deleteAll()
        userRepository.save(User("admin", "secret", Array(Role("ADMIN")).toList.asJava))


        monaSpectrumRestClient.login("admin", "secret")

      }
      "upload them to the server" in {

        writer.write(exampleRecords.toList.asJava)

        assert(monaSpectrumRestClient.count() == exampleRecords.length)
      }
    }
  }
}
