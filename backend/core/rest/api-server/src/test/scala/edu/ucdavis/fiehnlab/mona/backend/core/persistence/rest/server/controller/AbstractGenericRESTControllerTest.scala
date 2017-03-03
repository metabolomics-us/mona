package edu.ucdavis.fiehnlab.mona.backend.core.persistence.rest.server.controller

import com.fasterxml.jackson.databind.ObjectMapper
import com.jayway.restassured.RestAssured
import com.jayway.restassured.RestAssured._
import com.jayway.restassured.config.ObjectMapperConfig
import com.jayway.restassured.mapper.factory.Jackson2ObjectMapperFactory
import com.jayway.restassured.specification.RequestSpecification
import com.typesafe.scalalogging.LazyLogging
import edu.ucdavis.fiehnlab.mona.backend.core.auth.jwt.repository.UserRepository
import edu.ucdavis.fiehnlab.mona.backend.core.auth.types.{Role, User}
import edu.ucdavis.fiehnlab.mona.backend.core.domain.HelperTypes.LoginRequest
import edu.ucdavis.fiehnlab.mona.backend.core.domain.io.json.MonaMapper
import edu.ucdavis.fiehnlab.mona.backend.core.domain.service.LoginService
import org.scalatest.WordSpec
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.WebIntegrationTest
import org.springframework.http.MediaType

import scala.collection.JavaConverters._

/**
  * An abstract test to provides us with a simple way to test complex controllers
  * and all their operation including authorization
  */
abstract class AbstractGenericRESTControllerTest[TYPE](endpoint: String) extends AbstractSpringControllerTest {

  val requiresAuthForAllRequests: Boolean

  val saveRequiresAuthentication: Boolean = true

  /**
    * object to use for gets
    *
    * @return
    */
  def getValue: TYPE

  /**
    * returns an id for us for testing
    *
    * @return
    */
  def getId: String

  "after initializing the environment" when {

    RestAssured.baseURI = s"http://localhost:$port/rest"

    "A Rest Controller" should {
      "save may require authentication" in {
        if(saveRequiresAuthentication) {
          given().contentType("application/json; charset=UTF-8").body(getValue).when().post(s"$endpoint").`then`().statusCode(401)
          authenticate().contentType("application/json; charset=UTF-8").body(getValue).when().post(s"$endpoint").`then`().statusCode(200)
        } else {
          given().contentType("application/json; charset=UTF-8").body(getValue).when().post(s"$endpoint").`then`().statusCode(200)
        }
      }

      "searchCount" in {
        if(requiresAuthForAllRequests) {
          given().contentType("application/json; charset=UTF-8").when().get(s"$endpoint/count").`then`().statusCode(401)
          authenticate().contentType("application/json; charset=UTF-8").when().get(s"$endpoint/count").`then`().statusCode(200)
        } else {
          given().contentType("application/json; charset=UTF-8").when().get(s"$endpoint/count").`then`().statusCode(200)
        }
      }

      "get" in {
        if(requiresAuthForAllRequests) {
          given().log().all(true).contentType("application/json; charset=UTF-8").when().get(s"$endpoint/$getId").`then`().statusCode(401)
          authenticate().log().all(true).contentType("application/json; charset=UTF-8").when().get(s"$endpoint/$getId").`then`().statusCode(200)
        } else{
          given().log().all(true).contentType("application/json; charset=UTF-8").when().get(s"$endpoint/$getId").`then`().statusCode(200)
        }
      }

      "put requires authorization" in {
        given().log().all(true).contentType("application/json; charset=UTF-8").body(getValue).when().put(s"$endpoint/$getId").`then`().statusCode(401)

        authenticate().contentType("application/json; charset=UTF-8").body(getValue).when().put(s"$endpoint/$getId").`then`().statusCode(200)
      }

      "delete requires admin authorization" in {
        given().contentType("application/json; charset=UTF-8").delete(s"$endpoint/$getId").`then`().statusCode(401)

        authenticate("test", "test-secret").contentType("application/json; charset=UTF-8").when().delete(s"$endpoint/$getId").`then`().statusCode(403)

        authenticate().contentType("application/json; charset=UTF-8").when().delete(s"$endpoint/$getId").`then`().statusCode(200)
      }
    }
  }
}


/**
  * provides us with a simple, elegant way to refresh the application context between runs
  */
@WebIntegrationTest(Array("server.port=9999","eureka.client.enabled:false"))
abstract class AbstractSpringControllerTest extends WordSpec with LazyLogging {

  val port: Int = 9999

  @Autowired
  val userRepository: UserRepository = null

  @Autowired
  val loginService: LoginService = null

  /**
    * token based authorization is our default approach
    *
    * @param user
    * @param password
    * @return
    */
  def authenticate(user: String = "admin", password: String = "secret"): RequestSpecification = {
    val response = loginService.login(LoginRequest(user, password))

    assert(response.token != null)
    logger.debug(s"generated token is ${response.token}")
    given().contentType(MediaType.APPLICATION_JSON_VALUE).header("Authorization", s"Bearer ${response.token}")
  }


  "our first test set" must {
    "prepare the object mapper for rest assured" in {
      //configure the mapper for rest assured
      RestAssured.config = RestAssured.config().objectMapperConfig(ObjectMapperConfig.objectMapperConfig().jackson2ObjectMapperFactory(new Jackson2ObjectMapperFactory {
        override def create(aClass: Class[_], s: String): ObjectMapper = MonaMapper.create
      }))
    }

    "reset the user base" in {
      userRepository.deleteAll()
      userRepository.save(User("admin", "secret", Array(Role("ADMIN")).toList.asJava))
      userRepository.save(User("test", "test-secret"))
      userRepository.save(User("test2", "test-secret"))
    }
  }
}
