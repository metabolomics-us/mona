package edu.ucdavis.fiehnlab.mona.backend.services.downloader

import com.typesafe.scalalogging.LazyLogging
import edu.ucdavis.fiehnlab.mona.backend.core.amqp.event.config.{MonaNotificationBusConfiguration, MonaEventBusConfiguration}
import edu.ucdavis.fiehnlab.mona.backend.core.auth.jwt.config.JWTAuthenticationConfig
import edu.ucdavis.fiehnlab.mona.backend.core.auth.service.RestSecurityService
import edu.ucdavis.fiehnlab.mona.backend.core.persistence.mongo.config.MongoConfig
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.SpringApplication
import org.springframework.boot.autoconfigure.SpringBootApplication
import org.springframework.cloud.client.discovery.EnableDiscoveryClient
import org.springframework.context.annotation.Import
import org.springframework.core.annotation.Order
import org.springframework.http.HttpMethod
import org.springframework.security.config.annotation.web.builders.{HttpSecurity, WebSecurity}
import org.springframework.security.config.annotation.web.configuration.{WebSecurityConfigurerAdapter, EnableWebSecurity}
import org.springframework.security.config.http.SessionCreationPolicy

/**
  * Created by sajjan on 5/25/16.
  */
@SpringBootApplication
@Import(Array(classOf[MongoConfig], classOf[MonaEventBusConfiguration], classOf[MonaNotificationBusConfiguration]))
class Downloader extends WebSecurityConfigurerAdapter with LazyLogging {

}

object Downloader extends App {
  new SpringApplication(classOf[Downloader]).run()
}
