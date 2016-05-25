package edu.ucdavis.fiehnlab.mona.backend.services.repository.content

import java.io.File

import com.typesafe.scalalogging.LazyLogging
import org.springframework.beans.factory.annotation.Value
import org.springframework.boot.autoconfigure.AutoConfigureAfter
import org.springframework.boot.autoconfigure.web.{DispatcherServletAutoConfiguration, WebMvcAutoConfiguration}
import org.springframework.context.annotation.Configuration
import org.springframework.web.servlet.config.annotation.{EnableWebMvc, ResourceHandlerRegistry, WebMvcConfigurerAdapter}
import org.springframework.web.servlet.resource.{GzipResourceResolver, PathResourceResolver}

/**
  * Created by wohlgemuth on 5/24/16.
  */

@Configuration
//@AutoConfigureAfter(Array(classOf[DispatcherServletAutoConfiguration]))
class ContentConfig extends WebMvcConfigurerAdapter with LazyLogging {

  @Value("file://${mona.repository:#{systemProperties['java.io.tmpdir']}}mona/")
  val dir: String = ""

  override def addResourceHandlers(registry: ResourceHandlerRegistry) = {


    logger.info(s"configured directory ${dir} for storage of spectra objects")
    registry
      .addResourceHandler("/repository/**")
      .addResourceLocations(dir)
      .setCachePeriod(0)
      .resourceChain(true)
      .addResolver(new GzipResourceResolver())
      .addResolver(new PathResourceResolver())
  }
}
