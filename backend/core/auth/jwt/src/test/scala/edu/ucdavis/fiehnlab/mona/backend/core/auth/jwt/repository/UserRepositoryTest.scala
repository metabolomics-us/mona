package edu.ucdavis.fiehnlab.mona.backend.core.auth.jwt.repository


import edu.ucdavis.fiehnlab.mona.backend.core.auth.jwt.config.{EmbeddedAuthConfig, JWTAuthenticationConfig}
import edu.ucdavis.fiehnlab.mona.backend.core.domain.Users
import org.scalatest.wordspec.AnyWordSpec
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder
import org.springframework.test.context.{ActiveProfiles, TestContextManager}

/**
  *
  * Created by wohlgemuth on 3/24/16.
  */
@ActiveProfiles(Array("test"))
@SpringBootTest(classes = Array(classOf[EmbeddedAuthConfig], classOf[JWTAuthenticationConfig]))
class UserRepositoryTest extends AnyWordSpec {
  @Autowired
  val userRepository: UserRepository = null

  new TestContextManager(this.getClass).prepareTestInstance(this)

  "UserRepositoryTest" should {

    "findByUserName" in {
      userRepository.deleteAll()
      userRepository.save(new Users("test@gmail.com", "test"))

      assert(userRepository.count() == 1)
      assert(userRepository.findByEmailAddress("test@gmail.com") != null)
    }

    "hashed password should match" in {
      assert(new BCryptPasswordEncoder().matches("test", userRepository.findByEmailAddress("test@gmail.com").getPassword))
    }

    "don't rehash password upon resaving" in {
      val aUser = userRepository.findByEmailAddress("test@gmail.com")
      aUser.setEmailAddress("test2@gmail.com")
      userRepository.save(aUser)
      //userRepository.save(userRepository.findByUsername("test").copy(username = "test2"))
      assert(userRepository.findByEmailAddress("test@gmail.com").getPassword == userRepository.findByEmailAddress("test2@gmail.com").getPassword)
    }
  }
}
