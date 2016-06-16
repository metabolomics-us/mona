package edu.ucdavis.fiehnlab.mona.backend.core.auth.rest.controller

import java.io.InputStreamReader
import java.util.Collections

import com.jayway.restassured.RestAssured
import com.jayway.restassured.RestAssured._
import edu.ucdavis.fiehnlab.mona.backend.core.auth.jwt.config.JWTAuthenticationConfig
import edu.ucdavis.fiehnlab.mona.backend.core.auth.jwt.repository.UserRepository
import edu.ucdavis.fiehnlab.mona.backend.core.auth.rest.config.AuthSecurityConfig
import edu.ucdavis.fiehnlab.mona.backend.core.auth.types.{Role, User}
import edu.ucdavis.fiehnlab.mona.backend.core.domain.{Spectrum, Submitter}
import edu.ucdavis.fiehnlab.mona.backend.core.domain.io.json.JSONDomainReader
import edu.ucdavis.fiehnlab.mona.backend.core.persistence.mongo.repository.ISubmitterMongoRepository
import edu.ucdavis.fiehnlab.mona.backend.core.persistence.rest.server.controller.AbstractGenericRESTControllerTest
import org.junit.runner.RunWith
import org.scalatest.WordSpec
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.{SpringApplicationConfiguration, WebIntegrationTest}
import org.springframework.test.context.TestContextManager
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner

import scala.collection.JavaConverters._

/**
  * Created by wohlgemuth on 4/4/16.
  */
@RunWith(classOf[SpringJUnit4ClassRunner])
@SpringApplicationConfiguration(classes = Array(classOf[JWTAuthenticationConfig], classOf[TestConfig], classOf[AuthSecurityConfig]))
class UserControllerTest extends AbstractGenericRESTControllerTest[User]("/users") {

  //required for spring and scala tes
  new TestContextManager(this.getClass).prepareTestInstance(this)

  /**
    * object to use for gets
    *
    * @return
    */
  override def getValue: User = User("testadmin", "admin", List[Role](Role("ADMIN")).asJava)

  /**
    * returns an id for us for testing
    *
    * @return
    */
  override def getId: String = getValue.username

  override val requiresAuthForAllRequests: Boolean = true

  override val saveRequiresAuthentication: Boolean = false


  "UserController" should {
    RestAssured.baseURI = s"http://localhost:$port/rest"

    "save does not require authentication" in {
      given().contentType("application/json; charset=UTF-8").body(getValue).when().post(s"/users").then().statusCode(200)
    }

    "save should not allow a user to create an admin user" in {
      val user: User = given().contentType("application/json; charset=UTF-8").body(getValue).when().post(s"/users").then().statusCode(200).extract().body().as(classOf[User])

      assert(user.roles.asScala.forall(_.name != "ADMIN"))
    }

    "put should not allow a non-admin to add an admin role" in {
      val user: User = authenticate("testadmin", "admin").contentType("application/json; charset=UTF-8").body(getValue).when().put(s"/users/$getId").then().statusCode(200).extract().body().as(classOf[User])

      assert(user.roles.asScala.forall(_.name != "ADMIN"))
    }

    "put should be able to add admin role to a user if initiated by admin" in {
      val user: User = authenticate().contentType("application/json; charset=UTF-8").body(getValue).when().put(s"/users/$getId").then().statusCode(200).extract().body().as(classOf[User])

      assert(user.roles.asScala.exists(_.name == "ADMIN"))
    }
  }
}