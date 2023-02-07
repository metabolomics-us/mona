package edu.ucdavis.fiehnlab.mona.backend.core.persistence.rest.client.service

import com.typesafe.scalalogging.LazyLogging
import edu.ucdavis.fiehnlab.mona.backend.core.auth.jwt.config.JWTAuthenticationConfig
import edu.ucdavis.fiehnlab.mona.backend.core.auth.jwt.repository.UserRepository
import edu.ucdavis.fiehnlab.mona.backend.core.domain.HelperTypes.LoginRequest
import edu.ucdavis.fiehnlab.mona.backend.core.domain.service.LoginService
import edu.ucdavis.fiehnlab.mona.backend.core.domain.{Roles, Users}
import edu.ucdavis.fiehnlab.mona.backend.core.persistence.rest.client.config.RestClientTestConfig
import org.scalatest.wordspec.AnyWordSpec
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.boot.test.context.SpringBootTest.WebEnvironment
import org.springframework.test.annotation.DirtiesContext
import org.springframework.test.context.{ActiveProfiles, TestContextManager}

import scala.jdk.CollectionConverters._
import scala.util.Properties

/**
  * Created by wohlg on 3/27/2016.
  */
@SpringBootTest(classes = Array(classOf[RestClientTestConfig], classOf[JWTAuthenticationConfig]), webEnvironment = WebEnvironment.DEFINED_PORT)
@ActiveProfiles(Array("test", "mona.persistence", "mona.persistence.init"))
class RestLoginServiceTest extends AnyWordSpec with LazyLogging {

  val keepRunning: Boolean = Properties.envOrElse("keep.server.running", "false").toBoolean

  @Autowired
  val loginService: LoginService = null

  @Autowired
  val userRepo: UserRepository = null

  new TestContextManager(this.getClass).prepareTestInstance(this)

  "RestLoginServiceTest" should {
    "create user " in {
      userRepo.deleteAll()
      userRepo.save(new Users("admin", "secret", List(new Roles("ADMIN")).asJava))
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
      assert(info.emailAddress == "admin")
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
