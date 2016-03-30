package edu.ucdavis.fiehnlab.mona.backend.curation

import javax.inject.Qualifier

import edu.ucdavis.fiehnlab.mona.backend.core.auth.jwt.repository.UserRepository
import edu.ucdavis.fiehnlab.mona.backend.core.auth.jwt.service.MongoLoginService
import edu.ucdavis.fiehnlab.mona.backend.core.auth.types.{Role, User}
import edu.ucdavis.fiehnlab.mona.backend.core.domain.HelperTypes.LoginResponse
import edu.ucdavis.fiehnlab.mona.backend.core.domain.servcie.LoginService
import edu.ucdavis.fiehnlab.mona.backend.core.persistence.rest.client.api.MonaSpectrumRestClient
import edu.ucdavis.fiehnlab.mona.backend.core.persistence.rest.client.service.RestLoginService
import edu.ucdavis.fiehnlab.mona.backend.curation.reader.RestRepositoryReader
import edu.ucdavis.fiehnlab.mona.backend.curation.writer.RestRepositoryWriter
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.context.annotation.{Primary, Bean, ComponentScan, Configuration}

import scala.collection.JavaConverters._


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
  def loginService:LoginService = new MongoLoginService

  @Bean
  def loginResponse: LoginResponse = {
    userRepository.deleteAll()
    userRepository.save(User("admin", "secret", Array(Role("ADMIN")).toList.asJava))

    loginService.login("admin", "secret")
  }

  @Autowired
  val userRepository: UserRepository = null
}
