package edu.ucdavis.fiehnlab.mona.backend.services.downloader.scheduler.config

import edu.ucdavis.fiehnlab.mona.backend.core.amqp.event.config.MonaNotificationBusCounterConfiguration
import edu.ucdavis.fiehnlab.mona.backend.core.auth.jwt.service.PostgresLoginService
import edu.ucdavis.fiehnlab.mona.backend.core.domain.service.LoginService
import org.springframework.boot.autoconfigure.SpringBootApplication
import org.springframework.context.annotation.{Bean, Configuration, Import}


/**
  * Created by sajjan on 5/26/16.
  */
@SpringBootApplication(scanBasePackageClasses = Array())
@Import(Array(classOf[MonaNotificationBusCounterConfiguration]))
class TestConfig {

  @Bean
  def loginService: LoginService = new PostgresLoginService
}
