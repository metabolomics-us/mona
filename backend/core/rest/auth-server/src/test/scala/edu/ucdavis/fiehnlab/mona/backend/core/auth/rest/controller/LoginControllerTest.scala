package edu.ucdavis.fiehnlab.mona.backend.core.auth.rest.controller

import java.util.Date
import com.jayway.restassured.RestAssured
import com.jayway.restassured.RestAssured._
import com.typesafe.scalalogging.LazyLogging
import edu.ucdavis.fiehnlab.mona.backend.core.auth.jwt.config.JWTAuthenticationConfig
import edu.ucdavis.fiehnlab.mona.backend.core.auth.jwt.repository.IUserRepository
import edu.ucdavis.fiehnlab.mona.backend.core.auth.jwt.service.PostgresLoginService
import edu.ucdavis.fiehnlab.mona.backend.core.auth.jwt.types.TokenSecret
import edu.ucdavis.fiehnlab.mona.backend.core.auth.rest.config.AuthSecurityConfig
import edu.ucdavis.fiehnlab.mona.backend.core.domain.HelperTypes.{LoginInfo, LoginRequest, LoginResponse}
import edu.ucdavis.fiehnlab.mona.backend.core.domain.service.LoginService
import edu.ucdavis.fiehnlab.mona.backend.core.persistence.postgresql.config.PostgresqlConfiguration
import edu.ucdavis.fiehnlab.mona.backend.core.persistence.rest.server.controller.AbstractSpringControllerTest
import org.springframework.boot.autoconfigure.SpringBootApplication
import org.springframework.boot.web.server.LocalServerPort
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.boot.test.context.SpringBootTest.WebEnvironment
import org.springframework.context.annotation.{Bean, Import}
import org.springframework.test.context.{ActiveProfiles, TestContextManager}

/**
  * Created by wohlgemuth on 3/24/16.
  */
@SpringBootTest(classes = Array(classOf[TestConfig]), webEnvironment = WebEnvironment.DEFINED_PORT)
@ActiveProfiles(Array("test"))
class LoginControllerTest extends AbstractSpringControllerTest with LazyLogging {

  @LocalServerPort
  private val port = 0

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

          logger.info(s"${info.username}")
          logger.info(s"${info.roles}")
          assert(info.username == "admin")
          assert(info.roles.size() == 1)
          assert(info.roles.contains("ADMIN") == true)
        }

        "you need to be authenticated for extending tokens" in {
          logger.info(s"starting test")
          val response = given().contentType("application/json; charset=UTF-8").body(LoginRequest("admin", "secret")).when().post("/auth/login").`then`().statusCode(200).extract().body().as(classOf[LoginResponse])
          logger.info(s"${response}");
          given().contentType("application/json; charset=UTF-8").body(response).when().post("/auth/extend").`then`().statusCode(401).extract().body()//.as(classOf[LoginResponse])
        }

        "extend a token" in {
          //the new expiration must be atlest 8 year from now
          val expirationDate = new Date(System.currentTimeMillis() + (31556952000L * 8))
          val response = given().contentType("application/json; charset=UTF-8").body(LoginRequest("admin", "secret")).when().post("/auth/login").`then`().statusCode(200).extract().body().as(classOf[LoginResponse])
          val tenYearToken = authenticate().contentType("application/json; charset=UTF-8").body(response).when().post("/auth/extend").`then`().statusCode(200).extract().body().as(classOf[LoginResponse])
          val info = authenticate().contentType("application/json; charset=UTF-8").body(tenYearToken).when().post("/auth/info").`then`().statusCode(200).extract().body().as(classOf[LoginInfo])

          assert(info.username == "admin")
          assert(info.roles.size() == 1)
          assert(info.roles.contains("ADMIN") == true)

          logger.info(s"comparing ${info.validTo} vs $expirationDate")
          assert(expirationDate.before(info.validTo))
        }
      }
    }
  }
}

@SpringBootApplication(scanBasePackageClasses = Array())
@Import(Array(classOf[JWTAuthenticationConfig], classOf[AuthSecurityConfig], classOf[PostgresqlConfiguration]))
class TestConfig {

  /**
    * the service which actually does the login for us
    *
    * @return
    */
  @Bean
  def loginServiceDelegate: LoginService = new PostgresLoginService

  /**
    * the token secret used during the testing phase
    *
    * @return
    */
  @Bean
  def tokenSecret: TokenSecret = TokenSecret("sadaskdkljsalkd")
}
