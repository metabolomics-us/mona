package edu.ucdavis.fiehnlab.mona.backend.services.repository.content

import com.typesafe.scalalogging.LazyLogging
import org.springframework.beans.factory.annotation.Value
import org.springframework.boot.autoconfigure.AutoConfigureAfter
import org.springframework.boot.autoconfigure.web.{DispatcherServletAutoConfiguration, WebMvcAutoConfiguration}
import org.springframework.context.annotation.Configuration
import org.springframework.web.servlet.config.annotation.ResourceHandlerRegistry

/**
  * Created by wohlgemuth on 5/24/16.
  */

@Configuration
@AutoConfigureAfter(Array(classOf[DispatcherServletAutoConfiguration]))
class ContentConfig extends WebMvcAutoConfiguration.WebMvcAutoConfigurationAdapter with LazyLogging {

  @Value("${mona.repository:#{systemProperties['java.io.tmpdir']}}mona")
  val baseDir: String = ""


  override def addResourceHandlers(registry: ResourceHandlerRegistry): Unit = {
    super.addResourceHandlers(registry)
    logger.info(s"hosting files at: ${baseDir}")
    registry.addResourceHandler("/repository/**").addResourceLocations(s"file:///${baseDir}")
  }

}
