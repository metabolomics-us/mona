package edu.ucdavis.fiehnlab.mona.backend.core.auth.controller

import com.fasterxml.jackson.databind.ObjectMapper
import com.jayway.restassured.RestAssured
import com.jayway.restassured.RestAssured._
import com.jayway.restassured.config.ObjectMapperConfig
import com.jayway.restassured.mapper.factory.Jackson2ObjectMapperFactory
import edu.ucdavis.fiehnlab.mona.backend.core.auth.config.AuthenticationConfig
import edu.ucdavis.fiehnlab.mona.backend.core.auth.repository.UserRepository
import edu.ucdavis.fiehnlab.mona.backend.core.auth.types.{LoginResponse, LoginRequest, Role, User}
import edu.ucdavis.fiehnlab.mona.backend.core.domain.io.json.MonaMapper
import edu.ucdavis.fiehnlab.mona.backend.core.persistence.mongo.config.EmbeddedMongoDBConfiguration

import org.junit.runner.RunWith
import org.scalatest.WordSpec
import org.springframework.beans.factory.annotation.{Autowired, Value}
import org.springframework.boot.test.{WebIntegrationTest, SpringApplicationConfiguration}
import org.springframework.security.core.userdetails.UsernameNotFoundException
import org.springframework.test.context.TestContextManager
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner

import scala.collection.JavaConverters._

/**
  * Created by wohlgemuth on 3/24/16.
  */
@RunWith(classOf[SpringJUnit4ClassRunner])
@SpringApplicationConfiguration(classes = Array(classOf[EmbeddedMongoDBConfiguration], classOf[AuthenticationConfig]))
@WebIntegrationTest(Array("server.port=0"))
class LoginControllerTest extends WordSpec {


  @Value( """${local.server.port}""")
  val port: Int = 0

  @Autowired
  val userRepository: UserRepository = null

  new TestContextManager(this.getClass()).prepareTestInstance(this)

  "LoginControllerTest" should {

    RestAssured.config = RestAssured.config().objectMapperConfig(ObjectMapperConfig.objectMapperConfig().jackson2ObjectMapperFactory(new Jackson2ObjectMapperFactory {
      override def create(aClass: Class[_], s: String): ObjectMapper = MonaMapper.create
    }))

    RestAssured.baseURI = s"http://localhost:${port}/"

    "login" must {

      userRepository.deleteAll()
      userRepository.save(new User("admin", "password", List(Role("admin")).asJava))

      "create a token" in {

        val response = given().contentType("application/json; charset=UTF-8").body(LoginRequest("admin", "password")).when().post("/rest/auth/login").then().statusCode(200).extract().body().as(classOf[LoginResponse])

        assert(response.token != null)

      }
      "fail with an invalid user" in {

          given().contentType("application/json; charset=UTF-8").body(LoginRequest("admin2312", "password")).when().post("/rest/auth/login").then().statusCode(500)

      }
    }

  }
}
