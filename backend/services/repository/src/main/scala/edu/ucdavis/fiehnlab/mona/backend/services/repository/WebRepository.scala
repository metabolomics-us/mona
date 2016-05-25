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
import org.eclipse.jgit.api.Git
import org.eclipse.jgit.lib.Repository
import org.eclipse.jgit.storage.file.FileRepositoryBuilder
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
class WebRepository extends WebSecurityConfigurerAdapter with LazyLogging {

  override def configure(web: WebSecurity): Unit = {
    web.ignoring()
      //get is always available
      .antMatchers(HttpMethod.GET, "/**")
  }

  @Value("${mona.repository:#{systemProperties['java.io.tmpdir']}}/mona")
  val dir: String = null

  def localDirectory = new File(new File(this.dir),"repository")

  @Bean
  def fileLayout: FileLayout = {
    new SubmitterInchiKeySplashId(localDirectory)
  }

  /**
    * initializes a git repository for us
    * @return
    */
  @Bean
  def gitRepository: Repository = {

    if(!localDirectory.exists())
      localDirectory.mkdirs()

    val gitRepo = new File(localDirectory, ".git")

    if(gitRepo.exists()) {
      new FileRepositoryBuilder().setGitDir(gitRepo).build()
    } else {
      val repo = FileRepositoryBuilder.create(gitRepo)
      repo.create()
      repo
    }
  }

  @Bean
  def repositoryListener(eventBus: EventBus[Spectrum], layout: FileLayout): RepositoryListener = new RepositoryListener(eventBus, layout, new Git(gitRepository))
}

@Configuration
class ConfigureJetty extends LazyLogging{

  @Value("${mona.repository:#{systemProperties['java.io.tmpdir']}}mona")
  val dir: String = null

  def localDirectory = new File(new File(this.dir),"repository")

  @Bean
  def jetty:EmbeddedServletContainerFactory = {
    val factory = new JettyEmbeddedServletContainerFactory()
    factory.setRegisterDefaultServlet(false)
    factory
  }

  @Bean
  def servlet: ServletRegistrationBean = {
    logger.info(s"registering our servlet and using dir: $localDirectory")
    val servlet = new DefaultServlet
    val bean = new ServletRegistrationBean(servlet,"/repository/*")

    bean.addInitParameter("dirAllowed", "true")
    bean.addInitParameter("resourceBase",localDirectory.getAbsolutePath)
    bean.addInitParameter("pathInfoOnly","true")
    bean.setLoadOnStartup(1)
    bean.setEnabled(true)
    bean.setName("repository")
    bean.setAsyncSupported(false)
    bean.setOrder(1)

    bean
  }
}

object WebRepository extends App {
  new SpringApplication(classOf[WebRepository]).run()
}
