package edu.ucdavis.fiehnlab.mona.backend.core.auth.rest.controller

import com.jayway.restassured.RestAssured
import com.jayway.restassured.RestAssured._
import edu.ucdavis.fiehnlab.mona.backend.core.auth.jwt.config.JWTAuthenticationConfig
import edu.ucdavis.fiehnlab.mona.backend.core.auth.rest.config.AuthSecurityConfig
import edu.ucdavis.fiehnlab.mona.backend.core.auth.types.{Role, User}
import edu.ucdavis.fiehnlab.mona.backend.core.persistence.rest.server.controller.AbstractGenericRESTControllerTest
import org.junit.runner.RunWith
import org.springframework.boot.context.embedded.LocalServerPort
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.boot.test.context.SpringBootTest.WebEnvironment
import org.springframework.test.context.TestContextManager
import org.springframework.test.context.junit4.SpringRunner

import scala.collection.JavaConverters._

/**
  * Created by wohlgemuth on 4/4/16.
  */
@RunWith(classOf[SpringRunner])
@SpringBootTest(classes = Array(classOf[JWTAuthenticationConfig], classOf[TestConfig], classOf[AuthSecurityConfig]), webEnvironment = WebEnvironment.DEFINED_PORT)
class UserControllerTest extends AbstractGenericRESTControllerTest[User]("/users") {

  @LocalServerPort
  private val port = 0

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

  new TestContextManager(this.getClass).prepareTestInstance(this)

  "UserController" should {
    RestAssured.baseURI = s"http://localhost:$port/rest"

    "save does not require authentication" in {
      given().contentType("application/json; charset=UTF-8").body(getValue).when().post(s"/users").`then`().statusCode(200)
      userRepository.delete(getValue.username)
    }

    "save should not allow a user to create an admin user" in {
      val user: User = given().contentType("application/json; charset=UTF-8").body(getValue).when().post(s"/users").`then`().statusCode(200).extract().body().as(classOf[User])

      assert(user.roles.asScala.forall(_.name != "ADMIN"))
    }

    "put should not allow a non-admin to add an admin role" in {
      val user: User = authenticate("testadmin", "admin").contentType("application/json; charset=UTF-8").body(getValue).when().put(s"/users/$getId").`then`().statusCode(200).extract().body().as(classOf[User])

      assert(user.roles.asScala.forall(_.name != "ADMIN"))
    }

    "put should be able to add admin role to a user if initiated by admin" in {
      val user: User = authenticate().contentType("application/json; charset=UTF-8").body(getValue).when().put(s"/users/$getId").`then`().statusCode(200).extract().body().as(classOf[User])

      assert(user.roles.asScala.exists(_.name == "ADMIN"))
    }

    "save should not allow user registration to overwrite an existing account" in {
      val oldUser: User = authenticate().contentType("application/json; charset=UTF-8").when().get(s"/users/test").`then`().statusCode(200).extract().body().as(classOf[User])
      val user: User = User("test", "test")

      given().contentType("application/json; charset=UTF-8").log().all(true).body(user).when().post(s"/users").`then`().log().all(true).statusCode(409)

      assert(authenticate().contentType("application/json; charset=UTF-8").when().get(s"/users/test").`then`().statusCode(200).extract().body().as(classOf[User]) == oldUser)
    }
  }
}