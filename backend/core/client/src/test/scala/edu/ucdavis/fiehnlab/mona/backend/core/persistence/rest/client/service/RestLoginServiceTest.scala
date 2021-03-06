package edu.ucdavis.fiehnlab.mona.backend.core.persistence.rest.client.service

import com.typesafe.scalalogging.LazyLogging
import edu.ucdavis.fiehnlab.mona.backend.core.auth.jwt.config.JWTAuthenticationConfig
import edu.ucdavis.fiehnlab.mona.backend.core.auth.jwt.repository.UserRepository
import edu.ucdavis.fiehnlab.mona.backend.core.auth.types.{Role, User}
import edu.ucdavis.fiehnlab.mona.backend.core.domain.HelperTypes.LoginRequest
import edu.ucdavis.fiehnlab.mona.backend.core.domain.service.LoginService
import edu.ucdavis.fiehnlab.mona.backend.core.persistence.rest.client.config.RestClientTestConfig
import org.junit.runner.RunWith
import org.scalatest.WordSpec
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.boot.test.context.SpringBootTest.WebEnvironment
import org.springframework.test.context.TestContextManager
import org.springframework.test.context.junit4.SpringRunner

import scala.collection.JavaConverters._
import scala.util.Properties

/**
  * Created by wohlg on 3/27/2016.
  */
@RunWith(classOf[SpringRunner])
@SpringBootTest(classes = Array(classOf[RestClientTestConfig], classOf[JWTAuthenticationConfig]), webEnvironment = WebEnvironment.DEFINED_PORT)
class RestLoginServiceTest extends WordSpec with LazyLogging {

  val keepRunning: Boolean = Properties.envOrElse("keep.server.running", "false").toBoolean

  @Autowired
  val loginService: LoginService = null

  @Autowired
  val userRepo: UserRepository = null

  new TestContextManager(this.getClass).prepareTestInstance(this)

  "RestLoginServiceTest" should {
    "create user " in {
      userRepo.deleteAll()
      userRepo.save(User("admin", "secret", Array(Role("ADMIN")).toList.asJava))
    }

    "login service needs to be of correct class" in {
      assert(loginService.isInstanceOf[RestLoginService])
    }

    "login" in {
      val token = loginService.login(LoginRequest("admin", "secret"))
      logger.info(s"my token is ${token.token}")
    }

    "extend" in {
      val token = loginService.login(LoginRequest("admin", "secret"))
      val response = loginService.extend(token.token)
      assert(response.token != null)

      val info = loginService.info(token.token)
      assert(info.username == "admin")
    }

    "if specified the server should stay online, this can be done using the env variable 'keep.server.running=true' " in {
      if (keepRunning) {
        while (keepRunning) {
          logger.warn("waiting forever till you kill me!")
          Thread.sleep(300000); // Every 5 minutes
        }
      }
    }
  }
}
