package edu.ucdavis.fiehnlab.mona.backend.core.curation.runner

import edu.ucdavis.fiehnlab.mona.backend.core.auth.service.RestSecurityService
import edu.ucdavis.fiehnlab.mona.backend.curation.config.CurationConfig
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.SpringApplication
import org.springframework.boot.autoconfigure.SpringBootApplication
import org.springframework.cloud.client.discovery.EnableDiscoveryClient
import org.springframework.context.annotation.Import
import org.springframework.http.HttpMethod
import org.springframework.security.config.annotation.web.builders.{HttpSecurity, WebSecurity}
import org.springframework.security.config.annotation.web.configuration.{EnableWebSecurity, WebSecurityConfigurerAdapter}
import org.springframework.security.config.http.SessionCreationPolicy

/**
  * This class starts the curation service and let's it listen in the background for messages
  * it also exposes a couple of rest points, which allow simple scheduling of messages
  */
@SpringBootApplication
@EnableDiscoveryClient
@EnableWebSecurity
/***
  * the server depends on these configurations to wire all it's internal components together
  */
@Import(Array(classOf[CurationConfig]))
class CurationRunner  extends WebSecurityConfigurerAdapter {

}

/**
  * our local server, which should be connecting to eureka, etc
  */
object CurationRunner extends App{
  new SpringApplication(classOf[CurationRunner]).run()

}
