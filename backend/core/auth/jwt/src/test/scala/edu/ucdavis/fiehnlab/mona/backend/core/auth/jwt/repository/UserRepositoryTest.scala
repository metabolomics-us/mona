package edu.ucdavis.fiehnlab.mona.backend.core.auth.jwt.repository


import edu.ucdavis.fiehnlab.mona.backend.core.auth.jwt.config.EmbeddedAuthConfig
import edu.ucdavis.fiehnlab.mona.backend.core.auth.jwt.jwt.config.JWTAuthenticationConfig
import edu.ucdavis.fiehnlab.mona.backend.core.auth.jwt.jwt.repository.UserRepository
import edu.ucdavis.fiehnlab.mona.backend.core.auth.types.User
import org.junit.runner.RunWith
import org.scalatest.WordSpec
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.test.context.{ContextConfiguration, TestContextManager}
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner

import scala.collection.JavaConverters._
/**
  *
  * Created by wohlgemuth on 3/24/16.
  */
@RunWith(classOf[SpringJUnit4ClassRunner])
@ContextConfiguration(classes = Array(classOf[EmbeddedAuthConfig],classOf[JWTAuthenticationConfig]))
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
