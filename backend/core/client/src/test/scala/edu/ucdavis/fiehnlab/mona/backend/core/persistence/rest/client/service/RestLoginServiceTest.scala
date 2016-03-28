package edu.ucdavis.fiehnlab.mona.backend.core.persistence.rest.client.service

import edu.ucdavis.fiehnlab.mona.backend.core.auth.jwt.config.JWTAuthenticationConfig
import edu.ucdavis.fiehnlab.mona.backend.core.auth.jwt.repository.UserRepository
import edu.ucdavis.fiehnlab.mona.backend.core.auth.rest.config.AuthSecurityConfig
import edu.ucdavis.fiehnlab.mona.backend.core.auth.types.{Role, User}
import edu.ucdavis.fiehnlab.mona.backend.core.domain.HelperTypes.LoginRequest
import edu.ucdavis.fiehnlab.mona.backend.core.domain.servcie.LoginService
import edu.ucdavis.fiehnlab.mona.backend.core.persistence.rest.client.config.RestClientTestConfig
import org.junit.runner.RunWith
import org.scalatest.WordSpec
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.{SpringApplicationConfiguration, WebIntegrationTest}
import org.springframework.test.context.TestContextManager
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner

import scala.collection.JavaConverters._

/**
  * Created by wohlg on 3/27/2016.
  */
@RunWith(classOf[SpringJUnit4ClassRunner])
@SpringApplicationConfiguration(classes = Array(classOf[RestClientTestConfig],classOf[JWTAuthenticationConfig]))
@WebIntegrationTest(Array("server.port=44444"))
class RestLoginServiceTest extends WordSpec {

  @Autowired
  val loginService:LoginService = null

  @Autowired
  val userRepo: UserRepository = null

  //required for spring and scala tes
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
      loginService.login(LoginRequest("admin","secret"))
    }

  }
}
