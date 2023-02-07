package edu.ucdavis.fiehnlab.mona.backend.core.persistence.rest.server.controller.submitter

import java.io.InputStreamReader
import com.jayway.restassured.RestAssured
import com.jayway.restassured.RestAssured.given
import edu.ucdavis.fiehnlab.mona.backend.core.auth.jwt.config.JWTAuthenticationConfig
import edu.ucdavis.fiehnlab.mona.backend.core.domain.{Spectrum, SpectrumSubmitter, Submitter, Users}
import edu.ucdavis.fiehnlab.mona.backend.core.domain.io.json.JSONDomainReader
import edu.ucdavis.fiehnlab.mona.backend.core.persistence.postgresql.repository.{SpectrumRepository, SubmitterRepository}
import edu.ucdavis.fiehnlab.mona.backend.core.persistence.rest.server.AbstractGenericRESTControllerTest
import edu.ucdavis.fiehnlab.mona.backend.core.persistence.rest.server.config.{EmbeddedRestServerConfig, TestConfig}
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.web.server.LocalServerPort
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.boot.test.context.SpringBootTest.WebEnvironment
import org.springframework.test.context.{ActiveProfiles, TestContextManager}

/**
  * Created by sajjan on 3/9/16.
  */
@SpringBootTest(classes = Array(classOf[EmbeddedRestServerConfig], classOf[JWTAuthenticationConfig], classOf[TestConfig]), webEnvironment = WebEnvironment.DEFINED_PORT)
@ActiveProfiles(Array("test", "mona.persistence", "mona.persistence.init"))
class SubmitterRestControllerTest extends AbstractGenericRESTControllerTest[SpectrumSubmitter, String]("/submitters") {

  @LocalServerPort
  private val port = 0

  @Autowired
  val submitterRepository: SubmitterRepository = null

  @Autowired
  val spectrumRepository: SpectrumRepository = null

  new TestContextManager(this.getClass).prepareTestInstance(this)

  /**
   * object to use for gets
   *
   * @return
   */
  override def getValue: SpectrumSubmitter = {
    val temp = JSONDomainReader.create[Spectrum].read(new InputStreamReader(getClass.getResourceAsStream("/monaRecord.json")))
    temp.getSubmitter
  }

  /**
    * returns an id for us for testing
    *
    * @return
    */
  override def getId: String = getValue.getEmailAddress

  override val requiresAuthForAllRequests: Boolean = true

  override val deleteRequiresAuthentication: Boolean = false

  "we will be connecting to the REST controller" when {
    RestAssured.baseURI = s"http://localhost:$port/rest"

    "when connected we should be able to" should {
      val submitter: Submitter = new Submitter("test", "Test", "User", "UC Davis")

      "create a test submitter" in {
        spectrumRepository.deleteAll()
        submitterRepository.deleteAll()
        assert(submitterRepository.count() == 0)

        authenticate("test", "test-secret").contentType("application/json; charset=UTF-8").body(submitter).when().post(s"/submitters").`then`().statusCode(200)

        assert(submitterRepository.count() == 1)
        assert(submitterRepository.findTopByEmailAddress("test") != null)
      }

      "view one's own submitter" in {
        val result: Submitter = authenticate("test", "test-secret").contentType("application/json; charset=UTF-8").when().get(s"/submitters/test").`then`().statusCode(200).extract().as(classOf[Submitter])
        assert(result == submitter)
      }

      "not modify the submitter when authenticated as another user" in {
        val newSubmitter = new Submitter(submitter.getEmailAddress, submitter.getFirstName, submitter.getLastName, "UCSF")
        authenticate("test2", "test-secret").contentType("application/json; charset=UTF-8").body(newSubmitter).when().put(s"/submitters/test").`then`().statusCode(403)

        val result: Submitter = authenticate("test", "test-secret").contentType("application/json; charset=UTF-8").when().get(s"/submitters/test").`then`().statusCode(200).extract().as(classOf[Submitter])
        assert(result == submitter)
      }

      "update one's own submitter with matching user" in {
        val newSubmitter = new Submitter(submitter.getEmailAddress, submitter.getFirstName, submitter.getLastName, "UCSD")
        authenticate("test", "test-secret").contentType("application/json; charset=UTF-8").body(newSubmitter).when().put(s"/submitters/test").`then`().statusCode(200)

        val result: Submitter = authenticate("test", "test-secret").contentType("application/json; charset=UTF-8").when().get(s"/submitters/test").`then`().statusCode(200).extract().as(classOf[Submitter])
        assert(result == newSubmitter)
      }

      "cannot PUT to an id that does not match username" in {
        authenticate("test", "test-secret").contentType("application/json; charset=UTF-8").body(submitter).when().put(s"/submitters/new").`then`().statusCode(403)
      }

      "cannot view all submitters when not authenticated" in {
        given().when().contentType("application/json; charset=UTF-8").get(s"/submitters").`then`().statusCode(401)
      }

      "can list ones own submitter when authenticated" in {
        val result: Array[Submitter] = authenticate("test", "test-secret").when().contentType("application/json; charset=UTF-8").get(s"/submitters").`then`().statusCode(200).extract().as(classOf[Array[Submitter]])
        assert(result.length == 1)
        assert(result.head.getEmailAddress == "test")
        assert(result.head.getInstitution == "UCSD")
      }

      "can view ones own submitter when authenticated" in {
        val result: Submitter = authenticate("test", "test-secret").when().contentType("application/json; charset=UTF-8").get(s"/submitters/test").`then`().statusCode(200).extract().as(classOf[Submitter])
        assert(result.getEmailAddress == "test")
        assert(result.getInstitution == "UCSD")
      }

      "cannot list other's submitters when authenticated" in {
        val result: Array[Submitter] = authenticate("test2", "test-secret").when().contentType("application/json; charset=UTF-8").get(s"/submitters").`then`().statusCode(200).extract().as(classOf[Array[Submitter]])
        assert(result.isEmpty)

        authenticate("test2", "test-secret").when().contentType("application/json; charset=UTF-8").get(s"/submitters/test").`then`().statusCode(403)
      }

      "can list and view all submitters as admin" in {
        val result: Array[Submitter] = authenticate().when().contentType("application/json; charset=UTF-8").get(s"/submitters").`then`().statusCode(200).extract().as(classOf[Array[Submitter]])
        assert(result.length == 1)
        assert(result.head.getEmailAddress == "test")
        assert(result.head.getInstitution == "UCSD")

        authenticate().when().contentType("application/json; charset=UTF-8").get(s"/submitters/test").`then`().statusCode(200).extract().as(classOf[Submitter])
      }

      "handle full email addresses" should {
        "can create a test user with a full email address" in {
          userRepository.save(new Users("test@test.com", "test-secret"))
          submitterRepository.save(new Submitter("test@test.com",  "Test", "User", "UC Davis"))

          assert(userRepository.existsByEmailAddress("test@test.com"))
          assert(submitterRepository.existsByEmailAddress("test@test.com"))
        }

        "can access submitter information with full email address if logged in as that user" in {
          val result: Submitter = authenticate("test@test.com", "test-secret").when().contentType("application/json; charset=UTF-8").get(s"/submitters/test@test.com").`then`().statusCode(200).extract().as(classOf[Submitter])
          assert(result.getEmailAddress == "test@test.com")
          assert(result.getInstitution == "UC Davis")

          given().when().contentType("application/json; charset=UTF-8").get(s"/submitters/test@test.com").`then`().statusCode(401)
          authenticate("test", "test-secret").when().contentType("application/json; charset=UTF-8").get(s"/submitters/test@test.com").`then`().statusCode(403)
          authenticate().when().contentType("application/json; charset=UTF-8").get(s"/submitters/test@test.com").`then`().statusCode(200)
        }
      }
    }
  }
}
