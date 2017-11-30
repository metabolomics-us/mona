package edu.ucdavis.fiehnlab.mona.backend.core.auth.rest.controller

import java.util.Date

import com.jayway.restassured.RestAssured
import com.jayway.restassured.RestAssured._
import edu.ucdavis.fiehnlab.mona.backend.core.auth.jwt.config.JWTAuthenticationConfig
import edu.ucdavis.fiehnlab.mona.backend.core.auth.jwt.service.MongoLoginService
import edu.ucdavis.fiehnlab.mona.backend.core.auth.jwt.types.TokenSecret
import edu.ucdavis.fiehnlab.mona.backend.core.auth.rest.config.AuthSecurityConfig
import edu.ucdavis.fiehnlab.mona.backend.core.domain.HelperTypes.{LoginInfo, LoginRequest, LoginResponse}
import edu.ucdavis.fiehnlab.mona.backend.core.domain.service.LoginService
import edu.ucdavis.fiehnlab.mona.backend.core.persistence.mongo.config.MongoConfig
import edu.ucdavis.fiehnlab.mona.backend.core.persistence.rest.server.controller.AbstractSpringControllerTest
import org.junit.runner.RunWith
import org.springframework.boot.autoconfigure.SpringBootApplication
import org.springframework.boot.test.SpringApplicationConfiguration
import org.springframework.context.annotation.{Bean, Import}
import org.springframework.test.context.TestContextManager
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner

/**
  * Created by wohlgemuth on 3/24/16.
  */
@RunWith(classOf[SpringJUnit4ClassRunner])
@SpringApplicationConfiguration(classes = Array(classOf[JWTAuthenticationConfig], classOf[TestConfig], classOf[AuthSecurityConfig]))
class LoginControllerTest extends AbstractSpringControllerTest {

  new TestContextManager(this.getClass).prepareTestInstance(this)

  "LoginControllerTest" when {

    RestAssured.baseURI = s"http://localhost:$port/rest"

    "users were setup" must {

      "login" should {

        "create a token" in {

          val response = given().contentType("application/json; charset=UTF-8").body(LoginRequest("admin", "secret")).when().post("/auth/login").`then`().statusCode(200).extract().body().as(classOf[LoginResponse])

          assert(response.token != null)

        }
        "fail with an invalid user" in {
          given().contentType("application/json; charset=UTF-8").body(LoginRequest("hacker", "password")).when().post("/auth/login").`then`().statusCode(401).extract().statusLine().equals("Invalid username or password")
        }
        "fail with an invalid passwd" in {
          given().contentType("application/json; charset=UTF-8").body(LoginRequest("admin", "hacked")).when().post("/auth/login").`then`().statusCode(401).extract().statusLine().equals("Invalid username or password")
        }

        "you need to be authenticated for getting token infos" in {
          val response = given().contentType("application/json; charset=UTF-8").body(LoginRequest("admin", "secret")).when().post("/auth/login").`then`().statusCode(200).extract().body().as(classOf[LoginResponse])
          given().contentType("application/json; charset=UTF-8").body(response).when().post("/auth/info").`then`().statusCode(401)

        }
        "provide us with info for a token" in {
          val response = given().contentType("application/json; charset=UTF-8").body(LoginRequest("admin", "secret")).when().post("/auth/login").`then`().statusCode(200).extract().body().as(classOf[LoginResponse])


          val info = authenticate().contentType("application/json; charset=UTF-8").body(response).when().post("/auth/info").`then`().statusCode(200).extract().body().as(classOf[LoginInfo])


          assert(info.username == "admin")
          assert(info.roles.size() == 1)
          assert(info.roles.get(0) == "ADMIN")
        }

        "you need to be authenticated for extending tokens" in {


          val response = given().contentType("application/json; charset=UTF-8").body(LoginRequest("admin", "secret")).when().post("/auth/login").`then`().statusCode(200).extract().body().as(classOf[LoginResponse])
          given().contentType("application/json; charset=UTF-8").body(response).when().post("/auth/extend").`then`().statusCode(401).extract().body().as(classOf[LoginResponse])
        }

        "extend a token" in {
          //the new expiration must be atlest 8 year from now
          val expirationDate = new Date(System.currentTimeMillis() + (31556952000L * 8))

          val response = given().contentType("application/json; charset=UTF-8").body(LoginRequest("admin", "secret")).when().post("/auth/login").`then`().statusCode(200).extract().body().as(classOf[LoginResponse])

          val tenYearToken = authenticate().contentType("application/json; charset=UTF-8").body(response).when().post("/auth/extend").`then`().statusCode(200).extract().body().as(classOf[LoginResponse])

          val info = authenticate().contentType("application/json; charset=UTF-8").body(tenYearToken).when().post("/auth/info").`then`().statusCode(200).extract().body().as(classOf[LoginInfo])


          assert(info.username == "admin")
          assert(info.roles.size() == 1)
          assert(info.roles.get(0) == "ADMIN")

          logger.info(s"comparing ${info.validTo} vs $expirationDate")
          assert(expirationDate.before(info.validTo))
        }
      }
    }
  }
}

@SpringBootApplication
@Import(Array(classOf[MongoConfig]))
class TestConfig {

  /**
    * the service which actually does the login for us
    *
    * @return
    */
  @Bean
  def loginServiceDelegate: LoginService = new MongoLoginService


  /**
    * the token secret used during the testing phase
    *
    * @return
    */
  @Bean
  def tokenSecret: TokenSecret = TokenSecret("sadaskdkljsalkd")
}
