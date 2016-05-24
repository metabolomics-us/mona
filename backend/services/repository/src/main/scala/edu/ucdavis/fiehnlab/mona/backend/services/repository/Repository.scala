package edu.ucdavis.fiehnlab.mona.backend.services.repository

import java.io.{File, FileWriter, PrintWriter}
import javax.servlet.ServletContext

import com.typesafe.scalalogging.LazyLogging
import edu.ucdavis.fiehnlab.mona.backend.core.amqp.event.bus.EventBus
import edu.ucdavis.fiehnlab.mona.backend.core.amqp.event.config.{MonaEventBusConfiguration, MonaNotificationBusConfiguration}
import edu.ucdavis.fiehnlab.mona.backend.core.domain.Spectrum
import edu.ucdavis.fiehnlab.mona.backend.services.repository.layout.{FileLayout, SubmitterInchiKeySplashId}
import edu.ucdavis.fiehnlab.mona.backend.services.repository.listener.RepositoryListener
import org.eclipse.jetty.servlet.ServletHolder
import org.eclipse.jetty.webapp.WebAppContext
import org.springframework.beans.factory.annotation.Value
import org.springframework.boot.SpringApplication
import org.springframework.boot.autoconfigure.SpringBootApplication
import org.springframework.boot.context.embedded.ServletContextInitializer
import org.springframework.cloud.client.discovery.EnableDiscoveryClient
import org.springframework.context.annotation.{Bean, Configuration, Import}
import org.springframework.http.HttpMethod
import org.springframework.security.config.annotation.web.builders.WebSecurity
import org.springframework.security.config.annotation.web.configuration.WebSecurityConfigurerAdapter
import org.springframework.web.WebApplicationInitializer

/**
  * Created by wohlg_000 on 5/18/2016.
  */
@SpringBootApplication
@EnableDiscoveryClient
@Import(Array(classOf[MonaEventBusConfiguration], classOf[MonaNotificationBusConfiguration]))
class Repository extends LazyLogging {

  @Value("${mona.repository:#{systemProperties['java.io.tmpdir']}}mona")
  val baseDir: String = ""


  @Bean
  def fileLayout: FileLayout = {
    val dir = new File(this.baseDir)
    new SubmitterInchiKeySplashId(dir)
  }

  @Bean
  def repositoryListener(eventBus: EventBus[Spectrum], layout: FileLayout): RepositoryListener = new RepositoryListener(eventBus, layout)
}



@Configuration
class Security extends WebSecurityConfigurerAdapter {

  override def configure(web: WebSecurity): Unit = {
    web.ignoring()
      //get is always available
      .antMatchers(HttpMethod.GET, "/**")
  }

}

object Repository extends App {
  new SpringApplication(classOf[Repository]).run()
}
