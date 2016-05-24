package edu.ucdavis.fiehnlab.mona.backend.services.repository.content

import com.typesafe.scalalogging.LazyLogging
import org.eclipse.jetty.server.handler.ResourceHandler
import org.springframework.beans.factory.annotation.Value
import org.springframework.boot.autoconfigure.AutoConfigureAfter
import org.springframework.boot.autoconfigure.web.DispatcherServletAutoConfiguration
import org.springframework.context.annotation.Configuration
import org.springframework.web.servlet.config.annotation.{WebMvcConfigurerAdapter, ResourceHandlerRegistry}

/**
  * Created by wohlg_000 on 5/19/2016.
  */
@Configuration
@AutoConfigureAfter(Array(classOf[DispatcherServletAutoConfiguration]))
class ContentConfig extends WebMvcConfigurerAdapter with LazyLogging {

  @Value("${mona.repository:#{systemProperties['java.io.tmpdir']}}")
  val dir: String = ""

  override def addResourceHandlers(registry: ResourceHandlerRegistry) = {

    logger.info(s"configured directory ${dir} for storage of spectra objects")
    registry.addResourceHandler("/repository/**").addResourceLocations(s"file:///${dir}").setCachePeriod(0)
    super.addResourceHandlers(registry)
  }
}