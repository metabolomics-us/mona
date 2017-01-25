package edu.ucdavis.fiehnlab.mona.backend.core.auth.jwt.service

import edu.ucdavis.fiehnlab.mona.backend.core.auth.jwt.config.{EmbeddedAuthConfig, JWTAuthenticationConfig}
import edu.ucdavis.fiehnlab.mona.backend.core.auth.jwt.repository.UserRepository
import edu.ucdavis.fiehnlab.mona.backend.core.auth.types.User
import edu.ucdavis.fiehnlab.mona.backend.core.domain.HelperTypes.{LoginInfo, LoginResponse}
import edu.ucdavis.fiehnlab.mona.backend.core.domain.service.LoginService
import org.junit.runner.RunWith
import org.scalatest.WordSpec
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder
import org.springframework.test.context.{ContextConfiguration, TestContextManager, TestPropertySource}
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner

/**
  * Created by sajjan on 1/23/17.
  */
@RunWith(classOf[SpringJUnit4ClassRunner])
@ContextConfiguration(classes = Array(classOf[EmbeddedAuthConfig], classOf[JWTAuthenticationConfig]))
@TestPropertySource(locations=Array("classpath:application.properties"))
class MongoLoginServiceTest extends WordSpec {

  @Autowired
  val loginService: LoginService = null

  @Autowired
  val userRepository: UserRepository = null

  new TestContextManager(this.getClass).prepareTestInstance(this)

  "MongoLoginServiceTest" should {
    userRepository.deleteAll()
    userRepository.save(User("test", "test"))

    assert(userRepository.count() == 1)
    assert(userRepository.findByUsername("test") != null)

    "login" in {
      val response: LoginResponse = loginService.login("test", "test")
      assert(response != null)
      assert(response.token != null)
    }

    "get token info" in {
      val response: LoginResponse = loginService.login("test", "test")
      val info: LoginInfo = loginService.info(response.token)
      assert(info != null)
      assert(info.username == "test")
    }
  }
}
