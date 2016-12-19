package edu.ucdavis.fiehnlab.mona.app.server.proxy

import java.io.IOException

import com.netflix.zuul.ZuulFilter
import com.typesafe.scalalogging.LazyLogging
import edu.ucdavis.fiehnlab.mona.app.server.proxy.documentation.SwaggerRedirectFilter
import edu.ucdavis.fiehnlab.mona.app.server.proxy.logging.LoggableDispatcherServlet
import edu.ucdavis.fiehnlab.mona.backend.core.persistence.rest.SwaggerConfig
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.SpringApplication
import org.springframework.boot.autoconfigure.SpringBootApplication
import org.springframework.boot.autoconfigure.web.{DispatcherServletAutoConfiguration, ResourceProperties}
import org.springframework.boot.context.embedded.ServletRegistrationBean
import org.springframework.cloud.client.discovery.EnableDiscoveryClient
import org.springframework.cloud.context.config.annotation.RefreshScope
import org.springframework.cloud.netflix.zuul.EnableZuulProxy
import org.springframework.context.annotation.{Bean, Configuration, Import}
import org.springframework.core.annotation.Order
import org.springframework.core.io.Resource
import org.springframework.http.HttpMethod
import org.springframework.security.config.annotation.web.builders.WebSecurity
import org.springframework.security.config.annotation.web.configuration.WebSecurityConfigurerAdapter
import org.springframework.stereotype.Controller
import org.springframework.web.servlet.DispatcherServlet
import org.springframework.web.servlet.config.annotation.{CorsRegistry, ResourceHandlerRegistry, WebMvcConfigurerAdapter}
import org.springframework.web.servlet.resource.PathResourceResolver
import springfox.documentation.swagger2.annotations.EnableSwagger2

/**
  * Created by wohlgemuth on 3/28/16.
  */
@SpringBootApplication
@EnableDiscoveryClient
@EnableZuulProxy
@Controller
@RefreshScope
@EnableSwagger2
class ProxyServer {

  @Bean
  def rewriteFilter: ZuulFilter = {
    new SwaggerRedirectFilter()
  }

  @Bean
  def dispatcherRegistration: ServletRegistrationBean = {
    new ServletRegistrationBean(dispatcherServlet)
  }

  @Bean(name = Array(DispatcherServletAutoConfiguration.DEFAULT_DISPATCHER_SERVLET_BEAN_NAME))
  def dispatcherServlet: DispatcherServlet = {
    new LoggableDispatcherServlet()
  }
}

@Configuration
class CorsConfig extends WebMvcConfigurerAdapter with LazyLogging {
  @Autowired
  val resourceProperties: ResourceProperties = null

  override def addCorsMappings(registry: CorsRegistry): Unit = registry.addMapping("/**")

  override def addResourceHandlers(registry: ResourceHandlerRegistry): Unit = {
    // Add handler for all static files and REST endpoints
    registry.addResourceHandler("/**/*.css", "/**/*.html", "/**/*.js", "/**/*.json", "/**/*.jpg", "/**/*.jpeg",
        "/**/*.png", "/**/*.ttf", "/**/*.eot", "/**/*.svg", "/**/*.woff", "/**/*.woff2", "/rest/**")
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
  // System.setProperty("spring.config.name", "proxy-service");

  new SpringApplication(classOf[ProxyServer]).run()
}
