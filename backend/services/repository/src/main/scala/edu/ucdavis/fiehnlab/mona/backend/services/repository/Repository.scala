package edu.ucdavis.fiehnlab.mona.backend.services.repository

import java.io.File

import com.typesafe.scalalogging.LazyLogging
import edu.ucdavis.fiehnlab.mona.backend.core.amqp.event.bus.EventBus
import edu.ucdavis.fiehnlab.mona.backend.core.amqp.event.config.{MonaEventBusConfiguration, MonaNotificationBusConfiguration}
import edu.ucdavis.fiehnlab.mona.backend.core.auth.jwt.config.JWTAuthenticationConfig
import edu.ucdavis.fiehnlab.mona.backend.core.domain.Spectrum
import edu.ucdavis.fiehnlab.mona.backend.core.persistence.rest.SwaggerConfig
import edu.ucdavis.fiehnlab.mona.backend.services.repository.layout.{FileLayout, InchiKeyLayout}
import edu.ucdavis.fiehnlab.mona.backend.services.repository.listener.RepositoryListener
import org.eclipse.jetty.server.Server
import org.eclipse.jetty.server.handler.{ResourceHandler, ContextHandler}
import org.springframework.beans.factory.annotation.Value
import org.springframework.boot.autoconfigure.SpringBootApplication
import org.springframework.boot.context.embedded.{ConfigurableEmbeddedServletContainer, EmbeddedServletContainerCustomizer}
import org.springframework.boot.context.embedded.jetty.{JettyServerCustomizer, JettyEmbeddedServletContainerFactory}
import org.springframework.context.annotation.{Bean, Import}
import org.springframework.core.annotation.Order
import org.springframework.http.HttpMethod
import org.springframework.security.config.annotation.web.builders.WebSecurity
import org.springframework.security.config.annotation.web.configuration.{EnableWebSecurity, WebSecurityConfigurerAdapter}

/**
  * Created by wohlg_000 on 5/18/2016.
  */
@Order(1)
@EnableWebSecurity
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

  @Bean
  def jettyCustomizer(jetty: JettyEmbeddedServletContainerFactory): EmbeddedServletContainerCustomizer = {
    logger.info("configured jetty")
    new EmbeddedServletContainerCustomizer() {

      override def customize(configurableEmbeddedServletContainer: ConfigurableEmbeddedServletContainer): Unit = {
        val container = configurableEmbeddedServletContainer.asInstanceOf[JettyEmbeddedServletContainerFactory]

        container.addServerCustomizers(new JettyServerCustomizer {
          override def customize(server: Server): Unit = {

            val contextHandler = new ContextHandler("/repository")

            val handler = new ResourceHandler()
            handler.setDirectoriesListed(true)
            handler.setResourceBase(dir)
            contextHandler.setHandler(handler)

            server.setHandler(contextHandler)
            logger.info("configured embedded servlet container")
          }
        })
      }
    }
  }

  override def configure(web: WebSecurity): Unit = {
    web.ignoring()
      //get is always available
      .antMatchers(HttpMethod.GET, "/repository/**")
  }

  @Bean
  def repositoryListener(eventBus: EventBus[Spectrum], layout: FileLayout): RepositoryListener = new RepositoryListener(eventBus, layout)
}


object Repository {

}
