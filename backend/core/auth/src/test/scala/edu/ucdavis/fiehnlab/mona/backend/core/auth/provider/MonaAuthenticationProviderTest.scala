package edu.ucdavis.fiehnlab.mona.backend.core.auth.provider

import edu.ucdavis.fiehnlab.mona.backend.core.auth.config.{AuthenticationConfig, EmbeddedAuthConfig}
import edu.ucdavis.fiehnlab.mona.backend.core.auth.repository.UserRepository
import edu.ucdavis.fiehnlab.mona.backend.core.auth.types.{Role, User}
import edu.ucdavis.fiehnlab.mona.backend.core.persistence.mongo.config.EmbeddedMongoDBConfiguration
import org.junit.runner.RunWith
import org.scalatest.WordSpec
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.context.annotation.{Bean, Configuration}
import org.springframework.security.authentication.{BadCredentialsException, UsernamePasswordAuthenticationToken}
import org.springframework.security.core.userdetails.UsernameNotFoundException
import org.springframework.test.context.{ContextConfiguration, TestContextManager}
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner

import scala.collection.JavaConverters._
/**
  * Created by wohlgemuth on 3/24/16.
  */
@RunWith(classOf[SpringJUnit4ClassRunner])
@ContextConfiguration(classes = Array(classOf[EmbeddedAuthConfig]))
class MonaAuthenticationProviderTest extends WordSpec {

  @Autowired
  val authenticationProvider: MonaAuthenticationProvider = null

  @Autowired
  val authenticationRepository: UserRepository = null

  //required for spring and scala tes
  new TestContextManager(this.getClass()).prepareTestInstance(this)

  "MongoAuthenticationProviderTest" should {

    "authenticate - success - admin" in {
      authenticationRepository.deleteAll()

      authenticationRepository.save(User("admin", "12345", List(Role("admin")).asJava))
      authenticationRepository.save(User("user", "12345"))

      val result = authenticationProvider.authenticate(new UsernamePasswordAuthenticationToken("admin", "12345"))

      assert(result.getAuthorities.size() == 1)
      assert(result.getAuthorities.iterator().next().getAuthority == "admin")


    }

    "authenticate - success - user" in {

      authenticationRepository.deleteAll()
      authenticationRepository.save(User("user", "12345"))

      val result = authenticationProvider.authenticate(new UsernamePasswordAuthenticationToken("user", "12345"))

      assert(result.getAuthorities.size() == 0)

    }


    "throw an exception if a password and username is wrong" in {

      authenticationRepository.deleteAll()

      authenticationRepository.save(User("admin", "12345", List(Role("admin")).asJava))

      intercept[BadCredentialsException] {
        authenticationProvider.authenticate(new UsernamePasswordAuthenticationToken("admin", "thats not my password"))
      }
    }


    "throw an exception if a user is not found" in {

      authenticationRepository.deleteAll()

      intercept[UsernameNotFoundException] {
        authenticationProvider.authenticate(new UsernamePasswordAuthenticationToken("i'm a rabbit and not a user", "thats not my password"))
      }
    }


  }
}