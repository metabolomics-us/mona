package edu.ucdavis.fiehnlab.mona.backend.services.repository

import java.io.File
import javax.servlet.ServletContext

import com.typesafe.scalalogging.LazyLogging
import edu.ucdavis.fiehnlab.mona.backend.core.amqp.event.bus.EventBus
import edu.ucdavis.fiehnlab.mona.backend.core.amqp.event.config.{MonaEventBusConfiguration, MonaNotificationBusConfiguration}
import edu.ucdavis.fiehnlab.mona.backend.core.domain.Spectrum
import edu.ucdavis.fiehnlab.mona.backend.services.repository.layout.{FileLayout, SubmitterInchiKeySplashId}
import edu.ucdavis.fiehnlab.mona.backend.services.repository.listener.RepositoryListener
import org.eclipse.jetty.server.Server
import org.eclipse.jetty.servlet.{DefaultServlet, ServletHolder}
import org.springframework.beans.factory.annotation.Value
import org.springframework.boot.SpringApplication
import org.springframework.boot.autoconfigure.SpringBootApplication
import org.springframework.boot.context.embedded.jetty.{JettyEmbeddedServletContainerFactory, JettyServerCustomizer}
import org.springframework.boot.context.embedded._
import org.springframework.context.annotation.{Bean, Configuration, Import}
import org.springframework.http.HttpMethod
import org.springframework.security.config.annotation.web.builders.WebSecurity
import org.springframework.security.config.annotation.web.configuration.WebSecurityConfigurerAdapter
import org.springframework.web.WebApplicationInitializer
import org.springframework.web.context.WebApplicationContext

/**
  * Created by wohlg_000 on 5/18/2016.
  */
/**
  * Created by wohlg_000 on 5/18/2016.
  */
@SpringBootApplication
@Import(Array(classOf[MonaEventBusConfiguration], classOf[MonaNotificationBusConfiguration]))
class Repository extends WebSecurityConfigurerAdapter with LazyLogging {

  override def configure(web: WebSecurity): Unit = {
    web.ignoring()
      //get is always available
      .antMatchers(HttpMethod.GET, "/**")
  }

  @Bean
  def repositoryListener(eventBus: EventBus[Spectrum], layout: FileLayout): RepositoryListener = new RepositoryListener(eventBus, layout)

}

@Configuration
class ConfigureJetty extends LazyLogging{

  @Value("${mona.repository:#{systemProperties['java.io.tmpdir']}}mona")
  val dir: String = null

  @Bean
  def fileLayout: FileLayout = {
    val dir = new File(new File(this.dir),"repository")
    if (!dir.exists()) {
      logger.info(s"creating new mona repository ${dir} directory")
      dir.mkdirs()
    }
    new SubmitterInchiKeySplashId(dir)
  }

  @Bean
  def jetty:EmbeddedServletContainerFactory = {
    val root = new File(dir)
    logger.info(s"configured ${root} as root")
    val factory = new JettyEmbeddedServletContainerFactory()
    factory.setRegisterDefaultServlet(false)
    factory
  }

  @Bean
  def servlet: ServletRegistrationBean = {
    logger.info(s"registering our servlet and using dir: ${dir}")
    val servlet = new DefaultServlet
    val bean = new ServletRegistrationBean(servlet,"/repository/*")

    bean.addInitParameter("dirAllowed", "true")
    bean.addInitParameter("resourceBase",s"${dir}/repository/")
    bean.addInitParameter("pathInfoOnly","true")
    bean.setLoadOnStartup(1)
    bean.setEnabled(true)
    bean.setName("repository")
    bean.setAsyncSupported(false)
    bean.setOrder(1)

    bean

  }

}


object Repository extends App {
  new SpringApplication(classOf[Repository]).run()
}

