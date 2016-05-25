package edu.ucdavis.fiehnlab.mona.backend.services.repository

import java.io.File
import javax.servlet.ServletContext

import com.typesafe.scalalogging.LazyLogging
import edu.ucdavis.fiehnlab.mona.backend.core.amqp.event.bus.EventBus
import edu.ucdavis.fiehnlab.mona.backend.core.amqp.event.config.{MonaEventBusConfiguration, MonaNotificationBusConfiguration}
import edu.ucdavis.fiehnlab.mona.backend.core.domain.Spectrum
import edu.ucdavis.fiehnlab.mona.backend.services.repository.layout.{FileLayout, SubmitterInchiKeySplashId}
import edu.ucdavis.fiehnlab.mona.backend.services.repository.listener.RepositoryListener
import org.springframework.beans.factory.annotation.Value
import org.springframework.boot.SpringApplication
import org.springframework.boot.autoconfigure.SpringBootApplication
import org.springframework.boot.context.embedded.ServletContextInitializer
import org.springframework.context.annotation.{Bean, Import}
import org.springframework.http.HttpMethod
import org.springframework.security.config.annotation.web.builders.WebSecurity
import org.springframework.security.config.annotation.web.configuration.WebSecurityConfigurerAdapter

/**
  * Created by wohlg_000 on 5/18/2016.
  */
/**
  * Created by wohlg_000 on 5/18/2016.
  */
@SpringBootApplication
@Import(Array(classOf[MonaEventBusConfiguration], classOf[MonaNotificationBusConfiguration]))
class Repository extends WebSecurityConfigurerAdapter with LazyLogging {

  @Value("${mona.repository:#{systemProperties['java.io.tmpdir']}}mona")
  val dir: String = null

  @Bean
  def fileLayout: FileLayout = {
    val dir = new File(this.dir)
    if (!dir.exists()) {
      logger.info(s"creating new mona repository ${dir} directory")
      dir.mkdirs()
    }
    new SubmitterInchiKeySplashId(dir)
  }

  override def configure(web: WebSecurity): Unit = {
    web.ignoring()
      //get is always available
      .antMatchers(HttpMethod.GET, "/**")
  }

  @Bean
  def repositoryListener(eventBus: EventBus[Spectrum], layout: FileLayout): RepositoryListener = new RepositoryListener(eventBus, layout)

  @Bean
  def initializer:ServletContextInitializer = new ServletContextInitializer {
    override def onStartup(servletContext: ServletContext): Unit = {
      servletContext.setInitParameter("dirAllowed","true")
      servletContext.setInitParameter("org.eclipse.jetty.servlet.Default.dirAllowed","true")
    }
  }
}


object Repository extends App {
  new SpringApplication(classOf[Repository]).run()
}

