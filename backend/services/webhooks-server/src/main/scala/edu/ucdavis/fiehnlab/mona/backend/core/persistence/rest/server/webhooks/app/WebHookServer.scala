package edu.ucdavis.fiehnlab.mona.backend.core.persistence.rest.server.webhooks.app

import edu.ucdavis.fiehnlab.mona.backend.core.auth.jwt.config.JWTAuthenticationConfig
import edu.ucdavis.fiehnlab.mona.backend.core.persistence.rest.client.config.RestClientConfig
import edu.ucdavis.fiehnlab.mona.backend.core.persistence.rest.server.webhooks.config.WebHookSecurity
import edu.ucdavis.fiehnlab.mona.backend.core.persistence.rest.{EurekaClientConfig, SwaggerConfig}
import edu.ucdavis.fiehnlab.mona.backend.core.persistence.service.config.PersistenceServiceConfig
import org.springframework.boot.SpringApplication
import org.springframework.boot.autoconfigure.SpringBootApplication
import org.springframework.context.annotation.Import

/**
  * Created by wohlgemuth on 4/7/16.
  */
@SpringBootApplication
@Import(Array(classOf[WebHookSecurity], classOf[JWTAuthenticationConfig], classOf[SwaggerConfig],
  classOf[EurekaClientConfig], classOf[RestClientConfig], classOf[PersistenceServiceConfig]))
class WebHookServer

object WebHookServer extends App {
  new SpringApplication(classOf[WebHookServer]).run()
}