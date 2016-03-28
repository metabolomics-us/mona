package edu.ucdavis.fiehnlab.mona.backend.curation.reader

import java.io.InputStreamReader

import edu.ucdavis.fiehnlab.mona.backend.core.auth.jwt.config.JWTAuthenticationConfig
import edu.ucdavis.fiehnlab.mona.backend.core.auth.jwt.repository.UserRepository
import edu.ucdavis.fiehnlab.mona.backend.core.auth.types.{Role, User}
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
import scala.collection.JavaConverters._

/**
  * Created by wohlgemuth on 3/18/16.
  */
@RunWith(classOf[SpringJUnit4ClassRunner])
@SpringApplicationConfiguration(classes = Array(classOf[RestClientTestConfig], classOf[TestConfig], classOf[JWTAuthenticationConfig]))
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

  @Autowired
  val userRepository: UserRepository = null

  //required for spring and scala tes
  new TestContextManager(this.getClass()).prepareTestInstance(this)

  val exampleRecords: Array[Spectrum] = JSONDomainReader.create[Array[Spectrum]].read(new InputStreamReader(getClass.getResourceAsStream("/monaRecords.json")))

  "RestRepositoryReaderTest" should {

    "we need to login " in {
      userRepository.deleteAll()
      userRepository.save(User("admin", "secret", Array(Role("ADMIN")).toList.asJava))
      spectrumRestClient.login("admin", "secret")

    }

    "we need to prepare the database " in {

      spectrumRestClient.list().foreach(x =>
        spectrumRestClient.delete(x.id)
      )
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
  }
}
