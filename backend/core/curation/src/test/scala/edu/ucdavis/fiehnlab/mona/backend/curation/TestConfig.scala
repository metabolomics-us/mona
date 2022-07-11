package edu.ucdavis.fiehnlab.mona.backend.curation

import edu.ucdavis.fiehnlab.mona.backend.core.auth.jwt.repository.UserRepository
import edu.ucdavis.fiehnlab.mona.backend.core.auth.jwt.service.PostgresLoginService
import edu.ucdavis.fiehnlab.mona.backend.core.auth.types.{Role, User}
import edu.ucdavis.fiehnlab.mona.backend.core.domain.HelperTypes.LoginResponse
import edu.ucdavis.fiehnlab.mona.backend.core.domain.service.LoginService
import edu.ucdavis.fiehnlab.mona.backend.core.persistence.mongo.config.MongoConfig
import edu.ucdavis.fiehnlab.mona.backend.curation.reader.RestRepositoryReader
import edu.ucdavis.fiehnlab.mona.backend.curation.writer.RestRepositoryWriter
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.SpringBootConfiguration
import org.springframework.boot.autoconfigure.{EnableAutoConfiguration, SpringBootApplication}
import org.springframework.context.annotation.{Bean, ComponentScan, Configuration, Import}

import scala.jdk.CollectionConverters._


/**
  * Created by wohlg on 3/20/2016.
  */
@Configuration
@ComponentScan(Array("edu/ucdavis/fiehnlab/mona/backend/curation"))
class TestConfig {
  @Bean
  def restRepositoryReaderAll = new RestRepositoryReader()

  @Bean
  def restRepositoryReaderWithQuery = new RestRepositoryReader("""metaData=q='name=="ion mode" and value==negative'""")

  @Bean
  def restRepositoryWriter(token: LoginResponse) = new RestRepositoryWriter(token.token)

  @Bean
  def loginService: LoginService = new PostgresLoginService

  @Bean
  def loginResponse: LoginResponse = {
    userRepository.deleteAll()
    userRepository.save(User("admin", "secret", Array(Role("ADMIN")).toList.asJava))

    loginService.login("admin", "secret")
  }

  @Autowired
  val userRepository: UserRepository = null
}
