package edu.ucdavis.fiehnlab.mona.backend.core.auth.jwt.service

import edu.ucdavis.fiehnlab.mona.backend.core.auth.jwt.config.{EmbeddedAuthConfig, JWTAuthenticationConfig}
import edu.ucdavis.fiehnlab.mona.backend.core.auth.jwt.repository.UserRepository
import edu.ucdavis.fiehnlab.mona.backend.core.auth.types.User
import org.junit.runner.RunWith
import org.scalatest.WordSpec
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner
import org.springframework.test.context.{ContextConfiguration, TestContextManager, TestPropertySource}

/**
  * defines how to use a token service and ensure
  * that the generated tokens can be authenticated
  */
@RunWith(classOf[SpringJUnit4ClassRunner])
@ContextConfiguration(classes = Array(classOf[EmbeddedAuthConfig], classOf[JWTAuthenticationConfig]))
@TestPropertySource(locations = Array("classpath:application.properties"))
class JWTTokenServiceTest extends WordSpec {

  @Autowired
  val tokenService: JWTTokenService = null

  @Autowired
  val userRepository: UserRepository = null

  @Autowired
  val authenticationService: JWTAuthenticationService = null

  new TestContextManager(this.getClass).prepareTestInstance(this)

  "JWTTokenServiceTest" should {

    userRepository.deleteAll()
    userRepository.save(User("test", "test"))

    assert(userRepository.count() == 1)
    assert(userRepository.findByUsername("test") != null)

    "generateToken" in {
      val token = tokenService.generateToken(userRepository.findAll().iterator().next())

      assert(token != null)
      assert(authenticationService.authenticate(token).isAuthenticated)
    }

    "valid verify info" in {
      val token = tokenService.generateToken(userRepository.findAll().iterator().next())

      val info = tokenService.info(token)

      assert(info.username == "test")
      assert(info.roles.size() == 0)

      assert(info.validFrom != null)
      assert(info.validTo != null)
    }
  }
}
