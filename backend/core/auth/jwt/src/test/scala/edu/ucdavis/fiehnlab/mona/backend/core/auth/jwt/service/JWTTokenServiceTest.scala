package edu.ucdavis.fiehnlab.mona.backend.core.auth.jwt.service

import edu.ucdavis.fiehnlab.mona.backend.core.auth.jwt.config.{EmbeddedAuthConfig, JWTAuthenticationConfig}
import edu.ucdavis.fiehnlab.mona.backend.core.auth.jwt.repository.UserRepository
import edu.ucdavis.fiehnlab.mona.backend.core.domain.Users
import org.scalatest.wordspec.AnyWordSpec
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.test.context.{ActiveProfiles, TestContextManager, TestPropertySource}

/**
  * defines how to use a token service and ensure
  * that the generated tokens can be authenticated
  */
@ActiveProfiles(Array("test"))
@SpringBootTest(classes = Array(classOf[EmbeddedAuthConfig], classOf[JWTAuthenticationConfig]))
class JWTTokenServiceTest extends AnyWordSpec {
  @Autowired
  val tokenService: JWTTokenService = null

  @Autowired
  val userRepository: UserRepository = null

  @Autowired
  val authenticationService: JWTAuthenticationService = null

  new TestContextManager(this.getClass).prepareTestInstance(this)

  "JWTTokenServiceTest" should {

    userRepository.deleteAll()
    userRepository.save(new Users("test@gmail.com", "test"))

    assert(userRepository.count() == 1)
    assert(userRepository.findByEmailAddress("test@gmail.com") != null)

    "generateToken" in {
      val token = tokenService.generateToken(userRepository.findAll().iterator.next())

      assert(token != null)
      assert(authenticationService.authenticate(token).isAuthenticated)
    }

    "valid verify info" in {
      val token = tokenService.generateToken(userRepository.findAll().iterator.next())

      val info = tokenService.info(token)

      assert(info.emailAddress == "test@gmail.com")
      assert(info.roles.size() == 0)

      assert(info.validFrom != null)
      assert(info.validTo != null)
    }
  }
}
