package edu.ucdavis.fiehnlab.mona.backend.core.persistence.rest.server.webhooks.app

import edu.ucdavis.fiehnlab.mona.backend.core.auth.jwt.config.JWTAuthenticationConfig
import edu.ucdavis.fiehnlab.mona.backend.core.auth.jwt.types.TokenSecret
import edu.ucdavis.fiehnlab.mona.backend.core.persistence.rest.server.webhooks.config.WebHookSecurity
import org.springframework.beans.factory.annotation.Value
import org.springframework.boot.SpringApplication
import org.springframework.boot.autoconfigure.SpringBootApplication
import org.springframework.cloud.client.discovery.EnableDiscoveryClient
import org.springframework.context.annotation.{Bean, Import}
import org.springframework.scheduling.annotation.EnableAsync

/**
  * Created by wohlgemuth on 4/7/16.
  */
@SpringBootApplication
@EnableDiscoveryClient
@Import(Array(classOf[WebHookSecurity],classOf[JWTAuthenticationConfig]))
class WebHookServer

object WebHookServer extends App{
  new SpringApplication(classOf[WebHookServer]).run()

}