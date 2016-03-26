package edu.ucdavis.fiehnlab.mona.backend.core.persistence.rest.server.controller.auth

import com.jayway.restassured.RestAssured
import com.jayway.restassured.RestAssured._
import edu.ucdavis.fiehnlab.mona.backend.core.auth.config.AuthenticationConfig
import edu.ucdavis.fiehnlab.mona.backend.core.auth.repository.UserRepository
import edu.ucdavis.fiehnlab.mona.backend.core.auth.types.{Role, User}
import edu.ucdavis.fiehnlab.mona.backend.core.domain.HelperTypes.{LoginRequest, LoginResponse}
import edu.ucdavis.fiehnlab.mona.backend.core.persistence.mongo.config.EmbeddedMongoDBConfiguration
import edu.ucdavis.fiehnlab.mona.backend.core.persistence.rest.server.config.{EmbeddedRestServerConfig, JWTRestSecurityConfig}
import edu.ucdavis.fiehnlab.mona.backend.core.persistence.rest.server.controller.AbstractSpringControllerTest
import org.junit.runner.RunWith
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.{SpringApplicationConfiguration, WebIntegrationTest}
import org.springframework.test.context.TestContextManager
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner

import scala.collection.JavaConverters._

/**
  * Created by wohlgemuth on 3/24/16.
  */
@RunWith(classOf[SpringJUnit4ClassRunner])
@SpringApplicationConfiguration(classes = Array(classOf[JWTRestSecurityConfig], classOf[EmbeddedRestServerConfig]))
@WebIntegrationTest(Array("server.port=0"))
class LoginControllerTest extends AbstractSpringControllerTest {

  @Autowired
  val userRepository: UserRepository = null

  testContextManager.prepareTestInstance(this)

  "LoginControllerTest" when {

    RestAssured.baseURI = s"http://localhost:${port}/rest"

    "users were setup" must {
      userRepository.deleteAll()
      userRepository.save(new User("admin", "password", List(Role("admin")).asJava))

      "login" should {

        "create a token" in {

          val response = given().contentType("application/json; charset=UTF-8").body(LoginRequest("admin", "password")).when().post("/auth/login").then().statusCode(200).extract().body().as(classOf[LoginResponse])

          assert(response.token != null)

        }
        "fail with an invalid user" in {

          given().contentType("application/json; charset=UTF-8").body(LoginRequest("admin2312", "password")).when().post("/auth/login").then().statusCode(500)

        }
      }
    }
  }
}
