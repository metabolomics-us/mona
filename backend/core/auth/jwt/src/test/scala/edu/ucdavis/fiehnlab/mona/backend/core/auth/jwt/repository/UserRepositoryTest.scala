package edu.ucdavis.fiehnlab.mona.backend.core.auth.jwt.repository


import edu.ucdavis.fiehnlab.mona.backend.core.auth.jwt.config.{EmbeddedAuthConfig, JWTAuthenticationConfig}
import edu.ucdavis.fiehnlab.mona.backend.core.auth.types.User
import org.junit.runner.RunWith
import org.scalatest.WordSpec
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner
import org.springframework.test.context.{TestPropertySource, ContextConfiguration, TestContextManager}
/**
  *
  * Created by wohlgemuth on 3/24/16.
  */
@RunWith(classOf[SpringJUnit4ClassRunner])
@ContextConfiguration(classes = Array(classOf[EmbeddedAuthConfig],classOf[JWTAuthenticationConfig]))
@TestPropertySource(locations=Array("classpath:application.properties"))
class UserRepositoryTest extends WordSpec {

  @Autowired
  val userRepository: UserRepository = null

  new TestContextManager(this.getClass()).prepareTestInstance(this)

  "UserRepositoryTest" should {

    "findByUserName" in {
      userRepository.save(User("test", "test"))

      assert(userRepository.count() == 1)
      assert(userRepository.findByUsername("test") != null)
    }
  }
}
