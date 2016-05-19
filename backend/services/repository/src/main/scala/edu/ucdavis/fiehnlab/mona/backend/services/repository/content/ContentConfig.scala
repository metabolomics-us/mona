package edu.ucdavis.fiehnlab.mona.backend.services.repository.content

import java.io.FileNotFoundException

import com.typesafe.scalalogging.LazyLogging
import org.eclipse.jetty.server.Server
import org.eclipse.jetty.server.handler.{ContextHandler, ResourceHandler}
import org.springframework.beans.factory.annotation.Value
import org.springframework.boot.autoconfigure.AutoConfigureAfter
import org.springframework.boot.autoconfigure.web.{WebMvcAutoConfiguration, DispatcherServletAutoConfiguration}
import org.springframework.boot.context.embedded.{ConfigurableEmbeddedServletContainer, EmbeddedServletContainerCustomizer}
import org.springframework.boot.context.embedded.jetty.{JettyServerCustomizer, JettyEmbeddedServletContainerFactory}
import org.springframework.context.annotation.{Bean, Configuration}
import org.springframework.web.servlet.config.annotation.ResourceHandlerRegistry

/**
  * Created by wohlg_000 on 5/19/2016.
  */
//@Configuration
//@AutoConfigureAfter(Array(classOf[DispatcherServletAutoConfiguration]))
class ContentConfig extends LazyLogging {

  @Value("${mona.repository:#{systemProperties['java.io.tmpdir']}}")
  val dir: String = ""

  /*
    override def addResourceHandlers(registry: ResourceHandlerRegistry) = {

      val handler = new ResourceHandler()
      handler.setDirectoriesListed(true)

      logger.info(s"configured directory ${dir} for storage of spectra objects")
      registry.addResourceHandler("/repository/**").addResourceLocations(s"file:///${dir}").setCachePeriod(0)
      super.addResourceHandlers(registry)
    }
  */
  */
}
