package edu.ucdavis.fiehnlab.mona.backend.core.auth.rest.controller

import com.jayway.restassured.RestAssured
import com.jayway.restassured.RestAssured._
import edu.ucdavis.fiehnlab.mona.backend.core.domain.{Roles, Users}
import edu.ucdavis.fiehnlab.mona.backend.core.persistence.rest.server.AbstractGenericRESTControllerTest
import org.springframework.boot.web.server.LocalServerPort
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.boot.test.context.SpringBootTest.WebEnvironment
import org.springframework.test.context.{ActiveProfiles, TestContextManager}

import scala.jdk.CollectionConverters._

/**
  * Created by wohlgemuth on 4/4/16.
  */
@SpringBootTest(classes = Array(classOf[TestConfig]), webEnvironment = WebEnvironment.DEFINED_PORT)
@ActiveProfiles(Array("test"))
class UserControllerTest extends AbstractGenericRESTControllerTest[Users, String]("/users") {

  @LocalServerPort
  private val port = 0

  /**
    * object to use for gets
    *
    * @return
    */
  override def getValue: Users = new Users("testadmin", "admin", List(new Roles("ADMIN")).asJava)

  /**
    * returns an id for us for testing
    *
    * @return
    */
  override def getId: String = getValue.getEmailAddress

  override val requiresAuthForAllRequests: Boolean = true

  override val saveRequiresAuthentication: Boolean = false

  new TestContextManager(this.getClass).prepareTestInstance(this)

  "UserController" should {
    RestAssured.baseURI = s"http://localhost:$port/rest"

    "save does not require authentication" in {
      given().contentType("application/json; charset=UTF-8").body(getValue).when().post(s"/users").`then`().statusCode(200)
      userRepository.deleteByEmailAddress(getValue.getEmailAddress)
    }

    "save should not allow a user to create an admin user" in {
      val user: Users = given().contentType("application/json; charset=UTF-8").body(getValue).when().post(s"/users").`then`().statusCode(200).extract().body().as(classOf[Users])

      assert(user.getRoles.asScala.forall(_.getName != "ADMIN"))
    }

    "put should not allow a non-admin to add an admin role" in {
      val user: Users = authenticate("testadmin", "admin").contentType("application/json; charset=UTF-8").body(getValue).when().put(s"/users/$getId").`then`().statusCode(200).extract().body().as(classOf[Users])
      assert(user.getRoles.asScala.forall(_.getName != "ADMIN"))
    }


    "put should be able to add admin role to a user if initiated by admin" in {
      val user: Users = authenticate().contentType("application/json; charset=UTF-8").body(getValue).when().put(s"/users/$getId").`then`().statusCode(200).extract().body().as(classOf[Users])

      assert(user.getRoles.asScala.exists(_.getName == "ADMIN"))
    }

    "save should not allow user registration to overwrite an existing account" in {
      val oldUser: Users = authenticate().contentType("application/json; charset=UTF-8").when().get(s"/users/test").`then`().statusCode(200).extract().body().as(classOf[Users])
      val user: Users = new Users("test", "test")

      given().contentType("application/json; charset=UTF-8").log().all(true).body(user).when().post(s"/users").`then`().log().all(true).statusCode(409)

      assert(authenticate().contentType("application/json; charset=UTF-8").when().get(s"/users/test").`then`().statusCode(200).extract().body().as(classOf[Users]).getEmailAddress == oldUser.getEmailAddress)
    }
  }
}
