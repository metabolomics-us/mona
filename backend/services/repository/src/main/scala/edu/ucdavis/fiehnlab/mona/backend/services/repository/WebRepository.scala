package edu.ucdavis.fiehnlab.mona.backend.services.repository

import java.io.File
import javax.servlet.http.HttpServletRequest
import javax.servlet.{ServletContext, ServletRequest}

import com.typesafe.scalalogging.LazyLogging
import edu.ucdavis.fiehnlab.mona.backend.core.amqp.event.bus.EventBus
import edu.ucdavis.fiehnlab.mona.backend.core.amqp.event.config.{MonaEventBusConfiguration, MonaNotificationBusConfiguration}
import edu.ucdavis.fiehnlab.mona.backend.core.domain.Spectrum
import edu.ucdavis.fiehnlab.mona.backend.services.repository.layout.{FileLayout, SubmitterInchiKeySplashId}
import edu.ucdavis.fiehnlab.mona.backend.services.repository.listener.RepositoryListener
import org.eclipse.jetty.server.Server
import org.eclipse.jetty.servlet.{DefaultServlet, ServletHolder}
import org.eclipse.jgit.api.CreateBranchCommand.SetupUpstreamMode
import org.eclipse.jgit.api.Git
import org.eclipse.jgit.http.server.GitServlet
import org.eclipse.jgit.lib.{Repository, StoredConfig}
import org.eclipse.jgit.storage.file.FileRepositoryBuilder
import org.eclipse.jgit.transport.RemoteConfig
import org.eclipse.jgit.transport.resolver.RepositoryResolver
import org.springframework.beans.factory.annotation.Value
import org.springframework.boot.SpringApplication
import org.springframework.boot.autoconfigure.SpringBootApplication
import org.springframework.boot.context.embedded.jetty.{JettyEmbeddedServletContainerFactory, JettyServerCustomizer}
import org.springframework.boot.context.embedded._
import org.springframework.context.annotation.{Bean, Configuration, DependsOn, Import}
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
        .antMatchers(HttpMethod.GET,"/**")
      .antMatchers("/repository/**")
      .antMatchers("/git/*").anyRequest()
  }

  @Value("${mona.repository:#{systemProperties['java.io.tmpdir']}}#{systemProperties['file.separator']}mona")
  val dir: String = null

  def localDirectory = new File(new File(this.dir), "repository")

  @Bean
  def fileLayout: FileLayout = {
    new SubmitterInchiKeySplashId(localDirectory)
  }

  /**
    * initializes a git repository for us
    *
    * @return
    */
  @Bean
  def bareGitRepository: Git = {
    val bareDir = new File(dir)
    bareDir.mkdirs()

    val file = new File(bareDir, "repository.git")
    var git:Git = null

    if (!file.exists()) {
      logger.info(s"creating new git repository ${file}")
      git = Git.init().setDirectory(file).setBare(true).call()
    }
    else {
      logger.info(s"open existing repository ${file}")
      git = Git.open(file)
    }
    git
  }

  @Bean
  @DependsOn(Array("bareGitRepository"))
  def gitRepository(bareGitRepository: Git): Git = {

    if (localDirectory.exists()) {
      logger.info(s"opening checked out repository ${localDirectory}")
      Git.open(localDirectory)
    }
    else {
      logger.info("checking out remote repository")
      localDirectory.mkdirs()
      val uri = s"file://${dir}/repository.git"
      val git = Git.cloneRepository().setDirectory(localDirectory).setURI(uri).setCloneAllBranches(true).setBare(false).setRemote("origin/master").setBranch("master").call()

      git
    }
  }

  @Bean
  def repositoryListener(eventBus: EventBus[Spectrum], layout: FileLayout, gitRepository: Git): RepositoryListener = new RepositoryListener(eventBus, layout, gitRepository)
}

@Configuration
class ConfigureJetty extends LazyLogging {

  @Value("${mona.repository:#{systemProperties['java.io.tmpdir']}}#{systemProperties['file.separator']}mona")
  val dir: String = null

  def localDirectory = new File(new File(this.dir), "repository")

  @Bean
  def jetty: EmbeddedServletContainerFactory = {
    val factory = new JettyEmbeddedServletContainerFactory()
    factory.setRegisterDefaultServlet(false)
    factory
  }

  /**
    * provides us with browsing access to the repository
    * @return
    */
  @Bean
  def servlet: ServletRegistrationBean = {
    logger.info(s"registering our servlet and using dir: $localDirectory")
    val servlet = new DefaultServlet
    val bean = new ServletRegistrationBean(servlet, "/repository/*")

    bean.addInitParameter("dirAllowed", "true")
    bean.addInitParameter("resourceBase", localDirectory.getAbsolutePath)
    bean.addInitParameter("pathInfoOnly", "true")
    bean.setLoadOnStartup(1)
    bean.setEnabled(true)
    bean.setName("repository")
    bean.setAsyncSupported(false)
    bean.setOrder(1)

    bean
  }

  /**
    * provides us with access to the git repository to easily check it out
    * @param bareGitRepository
    * @return
    */
  @Bean
  def servletGit(bareGitRepository: Git): ServletRegistrationBean = {
    logger.info(s"registering our servlet and using dir: $localDirectory")
    val servlet = new GitServlet

    servlet.setRepositoryResolver(new RepositoryResolver[HttpServletRequest] {

      override def open(req: HttpServletRequest, name: String): Repository = {
        val repo = bareGitRepository.getRepository
        repo.incrementOpen()
        repo
      }
    })

    val bean = new ServletRegistrationBean(servlet, "/git/*")

    bean.addInitParameter("base-path", new File(dir).getAbsolutePath)
    bean.addInitParameter("export-all", "1")
    bean.setLoadOnStartup(1)
    bean.setEnabled(true)
    bean.setName("repository.git")
    bean.setAsyncSupported(false)
    bean.setOrder(1)

    bean
  }

}

object WebRepository extends App {
  new SpringApplication(classOf[WebRepository]).run()
}
