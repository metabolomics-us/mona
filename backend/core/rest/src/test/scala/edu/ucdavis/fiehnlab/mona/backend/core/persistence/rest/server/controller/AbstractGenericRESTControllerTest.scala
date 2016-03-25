package edu.ucdavis.fiehnlab.mona.backend.core.persistence.rest.server.controller

import com.fasterxml.jackson.databind.ObjectMapper
import com.jayway.restassured.RestAssured
import com.jayway.restassured.RestAssured._
import com.jayway.restassured.config.ObjectMapperConfig
import com.jayway.restassured.mapper.factory.Jackson2ObjectMapperFactory
import com.jayway.restassured.specification.RequestSpecification
import com.typesafe.scalalogging.LazyLogging
import edu.ucdavis.fiehnlab.mona.backend.core.auth.repository.UserRepository
import edu.ucdavis.fiehnlab.mona.backend.core.auth.types.{LoginRequest, LoginResponse, Role, User}
import edu.ucdavis.fiehnlab.mona.backend.core.domain.Spectrum
import edu.ucdavis.fiehnlab.mona.backend.core.domain.io.json.MonaMapper
import edu.ucdavis.fiehnlab.mona.backend.core.service.persistence.SpectrumPersistenceService
import org.scalatest.WordSpec
import org.springframework.beans.factory.annotation.{Autowired, Value}
import org.springframework.test.context.TestContextManager

import scala.collection.JavaConverters._

/**
  * an abstract test to provides us with a simple way to test complex controllers
  * and all their operation including authorization
  */
abstract class AbstractGenericRESTControllerTest[TYPE](endpoint:String) extends WordSpec with LazyLogging{

  /**
    * object to use for gets
    * @return
    */
  def getValue : TYPE

  /**
    * returns an id for us for testing
    * @return
    */
  def getId : String

  @Value( """${local.server.port}""")
  val port: Int = 0

  @Autowired
  val userRepository: UserRepository = null

  //required for spring and scala tes
  new TestContextManager(this.getClass()).prepareTestInstance(this)


  /**
    * token based authorization is our default approach
    * @param user
    * @param password
    * @return
    */
  def authenticate(user: String = "admin", password: String = "secret"): RequestSpecification = {
    val response = given().contentType("application/json; charset=UTF-8").body(LoginRequest(user, password)).when().post("/auth/login").then().statusCode(200).extract().body().as(classOf[LoginResponse])

    assert(response.token != null)
    logger.debug(s"generated token is ${response.token}")
    given().header("Authorization",s"Bearer ${response.token}")
  }


  "after initializing the environment" when {


    RestAssured.config = RestAssured.config().objectMapperConfig(ObjectMapperConfig.objectMapperConfig().jackson2ObjectMapperFactory(new Jackson2ObjectMapperFactory {
      override def create(aClass: Class[_], s: String): ObjectMapper = {
        logger.info("registering rest assured mapper")
        MonaMapper.create
      }
    }))

    RestAssured.baseURI = s"http://localhost:${port}/rest"

    "reset the user base" in {
      userRepository.deleteAll()
      userRepository.save(User("admin","secret",Array(Role("ADMIN")).toList.asJava))
      userRepository.save(User("test","test-secret"))
    }

    "A Rest Controller" should {

      "save requires authorization" in {
        given().contentType("application/json; charset=UTF-8").body(getValue).when().post(s"${endpoint}").then().statusCode(401)
        authenticate().contentType("application/json; charset=UTF-8").body(getValue).when().post(s"${endpoint}").then().statusCode(200)
      }

      "searchCount" in {
        given().contentType("application/json; charset=UTF-8").when().get(s"${endpoint}/count").then().statusCode(200)

      }

      "get" in {
        given().contentType("application/json; charset=UTF-8").when().get(s"${endpoint}/${getId}").then().statusCode(200)
      }

      "put requires authorization" in {
        given().contentType("application/json; charset=UTF-8").body(getValue).when().put(s"${endpoint}/${getId}").then().statusCode(401)

        authenticate().contentType("application/json; charset=UTF-8").body(getValue).when().put(s"${endpoint}/${getId}").then().statusCode(200)
      }

      "delete requires admin authorization" in {
        given().contentType("application/json; charset=UTF-8").delete(s"${endpoint}/${getId}").then().statusCode(401)

        authenticate("test","test-secret").contentType("application/json; charset=UTF-8").when().delete(s"${endpoint}/${getId}").then().statusCode(403)

        authenticate().contentType("application/json; charset=UTF-8").when().delete(s"${endpoint}/${getId}").then().statusCode(200)
      }

    }
  }
}
