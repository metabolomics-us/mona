package edu.ucdavis.fiehnlab.mona.backend.core.persistence.rest.server

import com.fasterxml.jackson.databind.ObjectMapper
import com.jayway.restassured.RestAssured
import com.jayway.restassured.RestAssured._
import com.jayway.restassured.config.ObjectMapperConfig
import com.jayway.restassured.mapper.factory.Jackson2ObjectMapperFactory
import com.jayway.restassured.specification.RequestSpecification
import com.typesafe.scalalogging.LazyLogging
import edu.ucdavis.fiehnlab.mona.backend.core.auth.jwt.repository.UserRepository
import edu.ucdavis.fiehnlab.mona.backend.core.domain.HelperTypes.LoginRequest
import edu.ucdavis.fiehnlab.mona.backend.core.domain.io.json.MonaMapper
import edu.ucdavis.fiehnlab.mona.backend.core.domain.service.LoginService
import edu.ucdavis.fiehnlab.mona.backend.core.domain.{Roles, Users}
import org.scalatest.wordspec.AnyWordSpec
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.http.MediaType

import scala.jdk.CollectionConverters._

/**
  * An abstract test to provides us with a simple way to test complex controllers
  * and all their operation including authorization
  */
abstract class AbstractGenericRESTControllerTest[TYPE, ID_TYPE](endpoint: String) extends AbstractSpringControllerTest {

  val requiresAuthForAllRequests: Boolean = false

  val saveRequiresAuthentication: Boolean = true

  val requiredAdminForWriting: Boolean = false

  val deleteRequiresAuthentication: Boolean = true

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
  def getId: ID_TYPE


  "after initializing the environment" when {
    "A Rest Controller" should {
      "save may require authentication" in {
        if (saveRequiresAuthentication) {
          given().contentType("application/json; charset=UTF-8").body(getValue).when().post(s"$endpoint").`then`().statusCode(401)

          authenticate().contentType("application/json; charset=UTF-8").body(getValue).when().post(s"$endpoint").`then`().statusCode(200)
        } else {
          given().contentType("application/json; charset=UTF-8").body(getValue).when().post(s"$endpoint").`then`().statusCode(200)
        }
      }

      "searchCount" in {
        if (requiresAuthForAllRequests) {
          given().contentType("application/json; charset=UTF-8").when().get(s"$endpoint/count").`then`().statusCode(401)

          authenticate().contentType("application/json; charset=UTF-8").when().get(s"$endpoint/count").`then`().statusCode(200)
        } else {
          given().contentType("application/json; charset=UTF-8").when().get(s"$endpoint/count").`then`().statusCode(200)
        }
      }

      "get" in {
        if (requiresAuthForAllRequests) {
          given().log().all(true).contentType("application/json; charset=UTF-8").when().get(s"$endpoint/$getId").`then`().statusCode(401)

          authenticate().log().all(true).contentType("application/json; charset=UTF-8").when().get(s"$endpoint/$getId").`then`().statusCode(200)
        } else {
          given().log().all(true).contentType("application/json; charset=UTF-8").when().get(s"$endpoint/$getId").`then`().statusCode(200)
        }
      }

      "put requires authorization" in {
        given().log().all(true).contentType("application/json; charset=UTF-8").body(getValue).when().put(s"$endpoint/$getId").`then`().statusCode(401)

        authenticate().contentType("application/json; charset=UTF-8").body(getValue).when().put(s"$endpoint/$getId").`then`().statusCode(200)

        if (requiredAdminForWriting) {
          authenticate("test", "test-secret").contentType("application/json; charset=UTF-8").body(getValue).when().put(s"$endpoint/$getId").`then`().statusCode(403)
        }
      }

      "delete may require authorization" in {
        if (deleteRequiresAuthentication) {
          given().contentType("application/json; charset=UTF-8").delete(s"$endpoint/$getId").`then`().statusCode(401)

          authenticate("test", "test-secret").contentType("application/json; charset=UTF-8").when().delete(s"$endpoint/$getId").`then`().statusCode(403)

          authenticate().contentType("application/json; charset=UTF-8").when().delete(s"$endpoint/$getId").`then`().statusCode(200)
        } else {
          given().contentType("application/json; charset=UTF-8").delete(s"$endpoint/$getId").`then`().statusCode(401)
          authenticate("test", "test-secret").contentType("application/json; charset=UTF-8").when().delete(s"$endpoint/$getId").`then`().statusCode(200)
        }
      }
    }
  }
}


/**
  * provides us with a simple, elegant way to refresh the application context between runs
  */
abstract class AbstractSpringControllerTest extends AnyWordSpec with LazyLogging {

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
    logger.debug(s"generated token for user $user is ${response.token}")
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

      val rolledUser = new Users("admin", "secret")
      rolledUser.setRoles(List(new Roles("ADMIN")).asJava)
      userRepository.save(rolledUser)
      userRepository.save(new Users("test", "test-secret"))
      userRepository.save(new Users("test2", "test-secret"))
    }
  }
}
