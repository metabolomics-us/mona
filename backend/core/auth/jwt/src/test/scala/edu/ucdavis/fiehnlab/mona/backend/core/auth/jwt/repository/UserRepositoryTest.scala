package edu.ucdavis.fiehnlab.mona.backend.core.auth.jwt.repository


import edu.ucdavis.fiehnlab.mona.backend.core.auth.jwt.config.{EmbeddedAuthConfig, JWTAuthenticationConfig}
import edu.ucdavis.fiehnlab.mona.backend.core.auth.types.User
import org.junit.runner.RunWith
import org.scalatest.WordSpec
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner
import org.springframework.test.context.{ContextConfiguration, TestContextManager, TestPropertySource}

/**
  *
  * Created by wohlgemuth on 3/24/16.
  */
@RunWith(classOf[SpringJUnit4ClassRunner])
@ContextConfiguration(classes = Array(classOf[EmbeddedAuthConfig], classOf[JWTAuthenticationConfig]))
@TestPropertySource(locations=Array("classpath:application.properties"))
class UserRepositoryTest extends WordSpec {

  @Autowired
  val userRepository: UserRepository = null

  new TestContextManager(this.getClass).prepareTestInstance(this)

  "UserRepositoryTest" should {

    "findByUserName" in {
      userRepository.deleteAll()
      userRepository.save(User("test", "test"))

      assert(userRepository.count() == 1)
      assert(userRepository.findByUsername("test") != null)
    }

    "hashed password should match" in {
      assert(new BCryptPasswordEncoder().matches("test", userRepository.findByUsername("test").password))
    }

    "don't rehash password upon resaving" in {
      userRepository.save(userRepository.findByUsername("test").copy(username = "test2"))
      assert(userRepository.findByUsername("test").password == userRepository.findByUsername("test2").password)
    }
  }
}
