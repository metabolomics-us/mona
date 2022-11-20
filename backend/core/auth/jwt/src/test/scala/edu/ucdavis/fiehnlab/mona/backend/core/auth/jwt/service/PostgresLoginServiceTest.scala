package edu.ucdavis.fiehnlab.mona.backend.core.auth.jwt.service

import com.typesafe.scalalogging.LazyLogging
import edu.ucdavis.fiehnlab.mona.backend.core.auth.jwt.config.{EmbeddedAuthConfig, JWTAuthenticationConfig}
import edu.ucdavis.fiehnlab.mona.backend.core.auth.jwt.repository.UserRepository
import edu.ucdavis.fiehnlab.mona.backend.core.domain.{Roles, Users}
import edu.ucdavis.fiehnlab.mona.backend.core.domain.HelperTypes.{LoginInfo, LoginResponse}
import edu.ucdavis.fiehnlab.mona.backend.core.domain.service.LoginService
import org.hibernate.Hibernate
import org.scalatest.BeforeAndAfterEach
import org.scalatest.wordspec.AnyWordSpec
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.test.context.{ActiveProfiles, TestContextManager}
import org.springframework.transaction.PlatformTransactionManager
import org.springframework.transaction.support.TransactionTemplate

import scala.jdk.CollectionConverters._

/**
  * Created by sajjan on 1/23/17.
  */
@ActiveProfiles(Array("test"))
@SpringBootTest(classes = Array(classOf[EmbeddedAuthConfig], classOf[JWTAuthenticationConfig]))
class PostgresLoginServiceTest extends AnyWordSpec with LazyLogging with BeforeAndAfterEach{

  @Autowired
  val loginService: LoginService = null

  @Autowired
  val userRepository: UserRepository = null

  @Autowired private val transactionManager: PlatformTransactionManager = null

  private var transactionTemplate: TransactionTemplate = null

  new TestContextManager(this.getClass).prepareTestInstance(this)

  protected override def beforeEach() = (
    transactionTemplate = new TransactionTemplate(transactionManager)
    )

  protected override def afterEach() = (
    transactionTemplate = null
    )

  "PostgresLoginServiceTest" should {
    userRepository.deleteAll()
    userRepository.save(new Users("test@gmail.com", "test"))

    assert(userRepository.count() == 1)
    assert(userRepository.findByEmailAddress("test@gmail.com") != null)

    "login" in {
      val response: LoginResponse = transactionTemplate.execute { x =>
        val z = loginService.login("test@gmail.com", "test")
        Hibernate.initialize(z)
        z
      }
      assert(response != null)
      assert(response.token != null)
    }

    "get token info" in {
      val response: LoginResponse = transactionTemplate.execute { x =>
        val z = loginService.login("test@gmail.com", "test")
        Hibernate.initialize(z)
        z
      }
      val info: LoginInfo = loginService.info(response.token)
      assert(info != null)
      assert(info.emailAddress == "test@gmail.com")
    }
  }
}
