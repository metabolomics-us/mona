package edu.ucdavis.fiehnlab.mona.backend.services.statistics.server.controller.config

import com.typesafe.scalalogging.LazyLogging
import edu.ucdavis.fiehnlab.mona.backend.core.auth.jwt.service.PostgresLoginService
import edu.ucdavis.fiehnlab.mona.backend.core.domain.service.LoginService
import edu.ucdavis.fiehnlab.mona.backend.services.statistics.server.StatisticServer
import org.springframework.context.annotation.{Bean, Configuration, Import}

/**
 * Created by wohlg on 3/11/2016.
 */
@Configuration
@Import(Array(classOf[StatisticServer]))
class EmbeddedRestServerConfig extends LazyLogging {

  /**
   * the service which actually does the login for us
   *
   * @return
   */
  @Bean
  def loginService: LoginService = new PostgresLoginService

}
