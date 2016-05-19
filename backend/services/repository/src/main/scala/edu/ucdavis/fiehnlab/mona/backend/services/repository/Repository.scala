package edu.ucdavis.fiehnlab.mona.backend.services.repository

import java.io.File

import com.typesafe.scalalogging.LazyLogging
import edu.ucdavis.fiehnlab.mona.backend.core.amqp.event.bus.EventBus
import edu.ucdavis.fiehnlab.mona.backend.core.amqp.event.config.{MonaEventBusConfiguration, MonaNotificationBusConfiguration}
import edu.ucdavis.fiehnlab.mona.backend.core.domain.Spectrum
import edu.ucdavis.fiehnlab.mona.backend.core.persistence.rest.SwaggerConfig
import edu.ucdavis.fiehnlab.mona.backend.services.repository.content.ContentCustomizer
import edu.ucdavis.fiehnlab.mona.backend.services.repository.layout.{FileLayout, InchiKeyLayout}
import edu.ucdavis.fiehnlab.mona.backend.services.repository.listener.RepositoryListener
import org.eclipse.jetty.server.handler.{ResourceHandler, ContextHandler}
import org.springframework.beans.factory.annotation.Value
import org.springframework.boot.SpringApplication
import org.springframework.boot.autoconfigure.SpringBootApplication
import org.springframework.boot.context.embedded.jetty.{JettyEmbeddedServletContainerFactory, JettyEmbeddedServletContainer}
import org.springframework.boot.context.embedded.{ConfigurableEmbeddedServletContainer, EmbeddedServletContainerCustomizer}
import org.springframework.context.annotation.{ComponentScan, Bean, Import}
import org.springframework.core.annotation.Order
import org.springframework.http.HttpMethod
import org.springframework.security.config.annotation.web.builders.WebSecurity
import org.springframework.security.config.annotation.web.configuration.WebSecurityConfigurerAdapter

/**
  * Created by wohlg_000 on 5/18/2016.
  */
@ComponentScan(basePackageClasses = Array(classOf[ContentCustomizer]))
@Order(1)
@SpringBootApplication
@Import(Array(classOf[MonaEventBusConfiguration], classOf[MonaNotificationBusConfiguration], classOf[SwaggerConfig]))
class Repository extends WebSecurityConfigurerAdapter with LazyLogging {

  @Value("${mona.repository:#{systemProperties['java.io.tmpdir']}}")
  val dir: String = null

  @Bean
  def fileLayout: FileLayout = {
    val dir = new File(this.dir)
    if (!dir.exists()) {
      logger.info(s"creating new mona repository ${dir} directory")
      dir.mkdirs()
    }
    new InchiKeyLayout(dir)
  }

  override def configure(web: WebSecurity): Unit = {
    web.ignoring()
      //get is always available
      .antMatchers(HttpMethod.GET, "/repository/**")
  }

  @Bean
  def repositoryListener(eventBus: EventBus[Spectrum], layout: FileLayout): RepositoryListener = new RepositoryListener(eventBus, layout)
}


object Repository extends App {
  new SpringApplication(classOf[Repository]).run()
}
