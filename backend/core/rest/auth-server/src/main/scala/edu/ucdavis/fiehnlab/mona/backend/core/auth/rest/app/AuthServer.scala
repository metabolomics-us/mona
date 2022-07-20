package edu.ucdavis.fiehnlab.mona.backend.core.auth.rest.app

import com.typesafe.scalalogging.LazyLogging
import edu.ucdavis.fiehnlab.mona.backend.core.auth.jwt.config.JWTAuthenticationConfig
import edu.ucdavis.fiehnlab.mona.backend.core.auth.jwt.repository.{IUserRepository, UserRepository}
import edu.ucdavis.fiehnlab.mona.backend.core.auth.jwt.service.PostgresLoginService
import edu.ucdavis.fiehnlab.mona.backend.core.auth.rest.config.AuthSecurityConfig
import edu.ucdavis.fiehnlab.mona.backend.core.persistence.postgresql.domain.{Roles, Users}
import edu.ucdavis.fiehnlab.mona.backend.core.domain.service.LoginService
import edu.ucdavis.fiehnlab.mona.backend.core.persistence.rest.{EurekaClientConfig, SwaggerConfig}
import org.springframework.beans.factory.annotation.{Autowired, Value}
import org.springframework.boot.autoconfigure.SpringBootApplication
import org.springframework.boot.{CommandLineRunner, SpringApplication}
import org.springframework.context.annotation.{Bean, Import}
import org.springframework.stereotype.Component

import scala.jdk.CollectionConverters._

/**
  * Created by wohlgemuth on 3/28/16.
  */
@SpringBootApplication
@Import(Array(classOf[AuthSecurityConfig], classOf[JWTAuthenticationConfig], classOf[SwaggerConfig], classOf[EurekaClientConfig]))
class AuthServer {

  /**
    * the service which actually does the login for us
    *
    * @return
    */
  @Bean
  def loginServiceDelegate: LoginService = new PostgresLoginService
}

@Component
class AuthCommandRunner extends CommandLineRunner with LazyLogging {

  @Value("${mona.security.auth.admin.username}")
  val adminUser: String = null

  @Value("${mona.security.auth.admin.password}")
  val adminPassword: String = null

  @Autowired
  val userRepository: UserRepository = null

  override def run(strings: String*): Unit = {
    if (userRepository.findByEmailAddress(adminUser) == null) {
      val newUser = new Users(adminUser, adminPassword)
      newUser.setRoles(List(new Roles("ADMIN")).asJava)
      userRepository.save(newUser)
      //val user = userRepository.save(new User(adminUser, adminPassword, List(new Roles("ADMIN")).asJava))
      logger.info(s"created default user: $newUser as admin, based on central credentials")
    } else {
      logger.info("utilizing existing user account")
    }
  }
}

object AuthServer extends App {
  new SpringApplication(classOf[AuthServer]).run()
}
