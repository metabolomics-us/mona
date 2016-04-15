package edu.ucdavis.fiehnlab.mona.backend.core.auth.rest.app

import com.typesafe.scalalogging.LazyLogging
import edu.ucdavis.fiehnlab.mona.backend.core.auth.jwt.config.JWTAuthenticationConfig
import edu.ucdavis.fiehnlab.mona.backend.core.auth.jwt.repository.UserRepository
import edu.ucdavis.fiehnlab.mona.backend.core.auth.jwt.service.MongoLoginService
import edu.ucdavis.fiehnlab.mona.backend.core.auth.jwt.types.TokenSecret
import edu.ucdavis.fiehnlab.mona.backend.core.auth.rest.config.AuthSecurityConfig
import edu.ucdavis.fiehnlab.mona.backend.core.auth.types.{Role, User}
import edu.ucdavis.fiehnlab.mona.backend.core.domain.service.LoginService
import org.springframework.beans.factory.annotation.{Autowired, Value}
import org.springframework.boot.autoconfigure.SpringBootApplication
import org.springframework.boot.{CommandLineRunner, SpringApplication}
import org.springframework.cloud.client.discovery.EnableDiscoveryClient
import org.springframework.context.annotation.{Bean, Import}
import org.springframework.stereotype.Component

import scala.collection.JavaConverters._

/**
  * Created by wohlgemuth on 3/28/16.
  */
@SpringBootApplication
@EnableDiscoveryClient
@Import(Array(classOf[AuthSecurityConfig],classOf[JWTAuthenticationConfig]))
class AuthServer {

  /**
    * the service which actually does the login for us
    *
    * @return
    */
  @Bean
  def loginServiceDelegate:LoginService = new MongoLoginService

}

@Component
class AuthCommandRunner extends CommandLineRunner with LazyLogging{

  @Value("${mona.security.auth.admin.username}")
  val adminUser:String = null

  @Value("${mona.security.auth.admin.password}")
  val adminPassword:String = null

  @Autowired
  val userRepository:UserRepository = null

  override def run(strings: String*): Unit = {
    val user = userRepository.save(User(adminUser,adminPassword, List(Role("admin")).asJava))
    logger.info(s"created default user: ${user}")
  }

}


object AuthServer extends App{
  new SpringApplication(classOf[AuthServer]).run()
}