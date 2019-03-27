package edu.ucdavis.fiehnlab.mona.app.server.proxy

import java.io.IOException
import javax.servlet.MultipartConfigElement

import com.netflix.zuul.ZuulFilter
import com.typesafe.scalalogging.LazyLogging
import edu.ucdavis.fiehnlab.mona.app.server.proxy.logging.{RequestLoggingFilter, LoggingService}
import edu.ucdavis.fiehnlab.mona.app.server.proxy.swagger.SwaggerRedirectFilter
import edu.ucdavis.fiehnlab.mona.backend.core.persistence.rest.{EurekaClientConfig, SwaggerConfig}
import org.springframework.beans.factory.annotation.{Autowired, Value}
import org.springframework.boot.SpringApplication
import org.springframework.boot.autoconfigure.SpringBootApplication
import org.springframework.boot.autoconfigure.web.{DispatcherServletAutoConfiguration, ResourceProperties}
import org.springframework.boot.web.servlet.{MultipartConfigFactory, ServletRegistrationBean}
import org.springframework.cloud.context.config.annotation.RefreshScope
import org.springframework.cloud.netflix.zuul.EnableZuulProxy
import org.springframework.context.annotation.{Bean, Configuration, Import}
import org.springframework.core.annotation.Order
import org.springframework.core.io.Resource
import org.springframework.security.config.annotation.web.builders.WebSecurity
import org.springframework.security.config.annotation.web.configuration.WebSecurityConfigurerAdapter
import org.springframework.stereotype.Controller
import org.springframework.web.servlet.DispatcherServlet
import org.springframework.web.servlet.config.annotation.{CorsRegistry, ResourceHandlerRegistry, WebMvcConfigurerAdapter}
import org.springframework.web.servlet.resource.PathResourceResolver

/**
  * Created by wohlgemuth on 3/28/16.
  */
@SpringBootApplication
@EnableZuulProxy
@Controller
@RefreshScope
@Import(Array(classOf[EurekaClientConfig]))
class ProxyServer {

  @Value("${spring.http.multipart.max-file-size}")
  val multipartMaxFileSize: String = null

  @Value("${spring.http.multipart.max-request-size}")
  val multipartMaxRequestSize: String = null

  @Bean(name = Array("multipartConfigElement"))
  def multipartConfigElement: MultipartConfigElement = {
    val factory: MultipartConfigFactory = new MultipartConfigFactory()
    factory.setMaxFileSize(multipartMaxFileSize)
    factory.setMaxRequestSize(multipartMaxRequestSize)
    factory.setLocation(System.getProperty("java.io.tmpdir"))
    factory.createMultipartConfig()
  }

  @Bean
  def rewriteFilter: ZuulFilter = {
    new SwaggerRedirectFilter
  }
}

@Configuration
class CorsConfig extends WebMvcConfigurerAdapter with LazyLogging {

  @Autowired
  val resourceProperties: ResourceProperties = null

  override def addCorsMappings(registry: CorsRegistry): Unit = registry.addMapping("/**")

  override def addResourceHandlers(registry: ResourceHandlerRegistry): Unit = {

    // Add handler for all static files and REST endpoints
    registry.addResourceHandler(
      // Web resources
      "/**/*.css", "/**/*.html", "/**/*.js", "/**/*.json", "/**/*.jpg",
      "/**/*.jpeg", "/**/*.png", "/**/*.ttf", "/**/*.eot", "/**/*.svg", "/**/*.woff", "/**/*.woff2",

      // API endpoints
      "/rest/**", "/**/v2/api-docs"
    )
      .addResourceLocations(resourceProperties.getStaticLocations: _*)
      .setCachePeriod(resourceProperties.getCachePeriod)

    // Handle all other paths with angular
    registry.addResourceHandler("/**")
      .addResourceLocations(resourceProperties.getStaticLocations.map(x => x + "index.html"): _*)
      .setCachePeriod(resourceProperties.getCachePeriod)
      .resourceChain(true)
      .addResolver(new PathResourceResolver() {
        @throws(classOf[IOException])
        override def getResource(resourcePath: String, location: Resource): Resource = {
          if (location.exists() && location.isReadable) location
          else null
        }
      })
  }
}

@Configuration
@Import(Array(classOf[SwaggerConfig]))
@Order(10)
class SecurityConfig extends WebSecurityConfigurerAdapter {

  override def configure(web: WebSecurity) {
    web.ignoring.antMatchers("/**")
  }
}

object ProxyServer extends App {
  new SpringApplication(classOf[ProxyServer]).run()
}
