package edu.ucdavis.fiehnlab.mona.backend.core.auth.rest.controller


import com.jayway.restassured.RestAssured
import com.jayway.restassured.RestAssured._
import edu.ucdavis.fiehnlab.mona.backend.core.auth.jwt.config.JWTAuthenticationConfig
import edu.ucdavis.fiehnlab.mona.backend.core.auth.jwt.repository.UserRepository
import edu.ucdavis.fiehnlab.mona.backend.core.auth.jwt.service.MongoLoginService
import edu.ucdavis.fiehnlab.mona.backend.core.auth.jwt.types.TokenSecret
import edu.ucdavis.fiehnlab.mona.backend.core.auth.rest.config.AuthSecurityConfig
import edu.ucdavis.fiehnlab.mona.backend.core.auth.types.{Role, User}
import edu.ucdavis.fiehnlab.mona.backend.core.domain.HelperTypes.{LoginInfo, LoginRequest, LoginResponse}
import edu.ucdavis.fiehnlab.mona.backend.core.domain.service.LoginService
import edu.ucdavis.fiehnlab.mona.backend.core.persistence.mongo.config.EmbeddedMongoDBConfiguration
import org.junit.runner.RunWith
import org.scalatest.WordSpec
import org.springframework.beans.factory.annotation.{Autowired, Value}
import org.springframework.boot.test.{SpringApplicationConfiguration, WebIntegrationTest}
import org.springframework.context.annotation.{Bean, Configuration, Import}
import org.springframework.test.context.TestContextManager
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner

import scala.collection.JavaConverters._

/**
  * Created by wohlgemuth on 3/24/16.
  */
@RunWith(classOf[SpringJUnit4ClassRunner])
@SpringApplicationConfiguration(classes = Array(classOf[JWTAuthenticationConfig], classOf[TestConfig],classOf[AuthSecurityConfig]))
@WebIntegrationTest(Array("server.port=0"))
class LoginControllerTest extends WordSpec {

  @Value( """${local.server.port}""")
  val port: Int = 0

  @Autowired
  val userRepository: UserRepository = null

  new TestContextManager(this.getClass()).prepareTestInstance(this)

  "LoginControllerTest" when {

    RestAssured.baseURI = s"http://localhost:${port}/rest"

    "users were setup" must {

      "login" should {

        "ensure we have a valid user" in {
          userRepository.deleteAll()
          userRepository.save(new User("admin", "password", List(Role("admin")).asJava))
        }

        "create a token" in {

          val response = given().contentType("application/json; charset=UTF-8").body(LoginRequest("admin", "password")).when().post("/auth/login").then().statusCode(200).extract().body().as(classOf[LoginResponse])

          assert(response.token != null)

        }
        "fail with an invalid user" in {

          given().contentType("application/json; charset=UTF-8").body(LoginRequest("admin2312", "password")).when().post("/auth/login").then().statusCode(500)

        }

        "provide us with info for a token" in {
          val response = given().contentType("application/json; charset=UTF-8").body(LoginRequest("admin", "password")).when().post("/auth/login").then().statusCode(200).extract().body().as(classOf[LoginResponse])

          val info  = given().contentType("application/json; charset=UTF-8").body(response).when().post("/auth/info/").then().statusCode(200).extract().body().as(classOf[LoginInfo])


          assert(info.username == "admin")
          assert(info.roles.size() == 1)
          assert(info.roles.get(0) == "admin")
        }
      }
    }
  }
}

@Configuration
@Import(Array(classOf[EmbeddedMongoDBConfiguration]))
class TestConfig {

  /**
    * the service which actually does the login for us
    *
    * @return
    */
  @Bean
  def loginServiceDelegate:LoginService = new MongoLoginService


  /**
    * the token secret used during the testing phase
    *
    * @return
    */
  @Bean
  def tokenSecret: TokenSecret = TokenSecret("sadaskdkljsalkd")

}
