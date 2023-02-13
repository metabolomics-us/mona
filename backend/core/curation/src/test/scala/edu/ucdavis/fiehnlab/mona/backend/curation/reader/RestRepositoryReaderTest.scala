package edu.ucdavis.fiehnlab.mona.backend.curation.reader

import java.io.InputStreamReader
import edu.ucdavis.fiehnlab.mona.backend.core.auth.jwt.config.JWTAuthenticationConfig
import edu.ucdavis.fiehnlab.mona.backend.core.auth.jwt.repository.UserRepository
import edu.ucdavis.fiehnlab.mona.backend.core.domain.{Roles, Spectrum, Users}
import edu.ucdavis.fiehnlab.mona.backend.core.domain.io.json.JSONDomainReader
import edu.ucdavis.fiehnlab.mona.backend.core.persistence.rest.client.api.GenericRestClient
import edu.ucdavis.fiehnlab.mona.backend.curation.TestConfig
import edu.ucdavis.fiehnlab.mona.backend.core.persistence.rest.client.config.RestClientTestConfig
import org.junit.runner.RunWith
import org.scalatest.wordspec.AnyWordSpec
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.boot.test.context.SpringBootTest.WebEnvironment
import org.springframework.test.context.{ActiveProfiles, TestContextManager}
import org.springframework.test.context.junit4.SpringRunner

import scala.jdk.CollectionConverters._

/**
  * Created by wohlgemuth on 3/18/16.
  */
@SpringBootTest(classes = Array(classOf[RestClientTestConfig], classOf[TestConfig], classOf[JWTAuthenticationConfig]), webEnvironment = WebEnvironment.DEFINED_PORT)
@ActiveProfiles(Array("test", "mona.persistence", "mona.persistence.init"))
class RestRepositoryReaderTest extends AnyWordSpec {


  @Autowired
  val spectrumRestClient: GenericRestClient[Spectrum, String] = null

  @Autowired
  val restRepositoryReaderAll: RestRepositoryReader = null

  @Autowired
  val restRepositoryReaderWithQuery: RestRepositoryReader = null

  @Autowired
  val userRepository: UserRepository = null

  new TestContextManager(this.getClass).prepareTestInstance(this)

  val exampleRecords: Array[Spectrum] = JSONDomainReader.create[Array[Spectrum]].read(new InputStreamReader(getClass.getResourceAsStream("/monaRecords.json")))

  "RestRepositoryReaderTest" should {

    "we need to login " in {
      userRepository.deleteAll()
      userRepository.save(new Users("admin", "secret", Array(new Roles("ADMIN")).toList.asJava))
      spectrumRestClient.login("admin", "secret")
    }

    "we need to prepare the database " in {
      spectrumRestClient.list().foreach(x => spectrumRestClient.delete(x.getId))
      exampleRecords.foreach(spectrumRestClient.add)
    }

    "read" in {
      var count = 0
      var data: Spectrum = null

      do {
        data = restRepositoryReaderAll.read()

        if (data != null) {
          count = count + 1
        }
      } while (data != null)

      assert(count == exampleRecords.length)
    }
  }
}
