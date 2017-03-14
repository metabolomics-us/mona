package edu.ucdavis.fiehnlab.mona.backend.core.persistence.rest.server.controller.submitter

import java.io.InputStreamReader

import com.fasterxml.jackson.databind.ObjectMapper
import com.jayway.restassured.RestAssured
import com.jayway.restassured.RestAssured.given
import com.jayway.restassured.config.ObjectMapperConfig
import com.jayway.restassured.mapper.factory.Jackson2ObjectMapperFactory
import edu.ucdavis.fiehnlab.mona.backend.core.auth.jwt.config.JWTAuthenticationConfig
import edu.ucdavis.fiehnlab.mona.backend.core.domain.io.json.{JSONDomainReader, MonaMapper}
import edu.ucdavis.fiehnlab.mona.backend.core.domain.{Spectrum, Submitter}
import edu.ucdavis.fiehnlab.mona.backend.core.persistence.mongo.repository.ISubmitterMongoRepository
import edu.ucdavis.fiehnlab.mona.backend.core.persistence.rest.server.config.{EmbeddedRestServerConfig, TestConfig}
import edu.ucdavis.fiehnlab.mona.backend.core.persistence.rest.server.controller.AbstractGenericRESTControllerTest
import org.junit.runner.RunWith
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.SpringApplicationConfiguration
import org.springframework.http.MediaType
import org.springframework.test.context.TestContextManager
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner

/**
  * Created by sajjan on 3/9/16.
  */
@RunWith(classOf[SpringJUnit4ClassRunner])
@SpringApplicationConfiguration(classes = Array(classOf[EmbeddedRestServerConfig],classOf[JWTAuthenticationConfig],classOf[TestConfig]))
class SubmitterRestControllerTest extends AbstractGenericRESTControllerTest[Submitter]("/submitters") {

  @Autowired
  val submitterRepository: ISubmitterMongoRepository = null

  //required for spring and scala tes
  new TestContextManager(this.getClass).prepareTestInstance(this)

  /**
    * object to use for gets
    *
    * @return
    */
  override def getValue: Submitter = JSONDomainReader.create[Spectrum].read(new InputStreamReader(getClass.getResourceAsStream("/monaRecord.json"))).submitter

  /**
    * returns an id for us for testing
    *
    * @return
    */
  override def getId: String = getValue.id

  override val requiresAuthForAllRequests: Boolean = true

  "we will be connecting to the REST controller" when {

    RestAssured.config = RestAssured.config().objectMapperConfig(ObjectMapperConfig.objectMapperConfig().jackson2ObjectMapperFactory(new Jackson2ObjectMapperFactory {
      override def create(aClass: Class[_], s: String): ObjectMapper = MonaMapper.create
    }))

    RestAssured.baseURI = s"http://localhost:$port/rest"

    "when connected we should be able to" should {
      val submitter: Submitter = Submitter("test", "test", "Test", "User", "UC Davis")

      "create a test submitter" in {
        submitterRepository.deleteAll()
        assert(submitterRepository.count() == 0)

        authenticate("test", "test-secret").contentType("application/json; charset=UTF-8").body(submitter).when().post(s"/submitters").`then`().statusCode(200)

        assert(submitterRepository.count() == 1)
        assert(submitterRepository.findByEmailAddress("test") != null)
      }

      "view one's own submitter" in {
        val result: Submitter = authenticate("test", "test-secret").contentType("application/json; charset=UTF-8").when().get(s"/submitters/test").`then`().statusCode(200).extract().as(classOf[Submitter])
        assert(result == submitter)
      }

      "not modify the submitter when authenticated as another user" in {
        val newSubmitter = submitter.copy(institution = "UCSF")
        authenticate("test2", "test-secret").contentType("application/json; charset=UTF-8").body(newSubmitter).when().put(s"/submitters/test").`then`().statusCode(403)

        val result: Submitter = authenticate("test", "test-secret").contentType("application/json; charset=UTF-8").when().get(s"/submitters/test").`then`().statusCode(200).extract().as(classOf[Submitter])
        assert(result == submitter)
      }

      "update one's own submitter with matching user" in {
        val newSubmitter = submitter.copy(institution = "UCSD")
        authenticate("test", "test-secret").contentType("application/json; charset=UTF-8").body(newSubmitter).when().put(s"/submitters/test").`then`().statusCode(200)

        val result: Submitter = authenticate("test", "test-secret").contentType("application/json; charset=UTF-8").when().get(s"/submitters/test").`then`().statusCode(200).extract().as(classOf[Submitter])
        assert(result == newSubmitter)
      }

      "cannot PUT to an id that does not match username" in {
        authenticate("test", "test-secret").contentType("application/json; charset=UTF-8").body(submitter).when().put(s"/submitters/new").`then`().statusCode(403)
      }

      "cannot view all submitters when not authenticated" in {
        given().when().contentType("application/json; charset=UTF-8").get(s"/submitters").then().statusCode(401)
      }

      "can list ones own submitter when authenticated" in {
        val result: Array[Submitter] = authenticate("test", "test-secret").when().contentType("application/json; charset=UTF-8").get(s"/submitters").`then`().statusCode(200).extract().as(classOf[Array[Submitter]])
        assert(result.length == 1)
        assert(result.head.id == "test")
        assert(result.head.institution == "UCSD")
      }

      "can view ones own submitter when authenticated" in {
        val result: Submitter = authenticate("test", "test-secret").when().contentType("application/json; charset=UTF-8").get(s"/submitters/test").`then`().statusCode(200).extract().as(classOf[Submitter])
        assert(result.id == "test")
        assert(result.institution == "UCSD")
      }

      "cannot list other's submitters when authenticated" in {
        val result: Array[Submitter] = authenticate("test2", "test-secret").when().contentType("application/json; charset=UTF-8").get(s"/submitters").`then`().statusCode(200).extract().as(classOf[Array[Submitter]])
        assert(result.isEmpty)

        authenticate("test2", "test-secret").when().contentType("application/json; charset=UTF-8").get(s"/submitters/test").`then`().statusCode(403)
      }

      "can list and view all submitters as admin" in {
        val result: Array[Submitter] = authenticate().when().contentType("application/json; charset=UTF-8").get(s"/submitters").`then`().statusCode(200).extract().as(classOf[Array[Submitter]])
        assert(result.length == 1)
        assert(result.head.id == "test")
        assert(result.head.institution == "UCSD")

        authenticate().when().contentType("application/json; charset=UTF-8").get(s"/submitters/test").`then`().statusCode(200).extract().as(classOf[Submitter])
      }
    }
  }
}
