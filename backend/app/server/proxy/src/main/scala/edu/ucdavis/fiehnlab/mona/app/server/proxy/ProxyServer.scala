package edu.ucdavis.fiehnlab.mona.app.server.proxy

import java.io.IOException
import javax.servlet.MultipartConfigElement
import com.typesafe.scalalogging.LazyLogging
import edu.ucdavis.fiehnlab.mona.backend.core.persistence.rest.{EurekaClientConfig, SwaggerConfig}
import org.springframework.beans.factory.annotation.{Autowired, Value}
import org.springframework.boot.SpringApplication
import org.springframework.boot.autoconfigure.SpringBootApplication
import org.springframework.boot.autoconfigure.web.WebProperties.Resources
import org.springframework.boot.web.servlet.MultipartConfigFactory
import org.springframework.cloud.context.config.annotation.RefreshScope
import org.springframework.context.annotation.{Bean, Configuration, Import, Lazy}
import org.springframework.core.annotation.Order
import org.springframework.core.io.Resource
import org.springframework.security.config.annotation.web.builders.{HttpSecurity, WebSecurity}
import org.springframework.security.config.annotation.web.configuration.WebSecurityConfigurerAdapter
import org.springframework.stereotype.Controller
import org.springframework.util.unit.DataSize
import org.springframework.web.servlet.config.annotation.{CorsRegistry, ResourceHandlerRegistry, WebMvcConfigurer, WebMvcConfigurerAdapter}
import org.springframework.web.servlet.resource.PathResourceResolver
import org.springframework.boot.autoconfigure.domain.EntityScan
import org.springframework.data.jpa.repository.config.EnableJpaRepositories
import org.springframework.boot.autoconfigure.security.reactive.ReactiveSecurityAutoConfiguration
import org.springframework.boot.actuate.autoconfigure.security.reactive.ReactiveManagementWebSecurityAutoConfiguration
import org.springframework.cloud.gateway.route.{RouteDefinition, RouteDefinitionLocator}
import org.springframework.web.filter.ForwardedHeaderFilter

import scala.collection.mutable.Buffer
import scala.collection.mutable.ListBuffer
import scala.jdk.CollectionConverters._

/**
  * Created by wohlgemuth on 3/28/16.
  */
@SpringBootApplication(exclude = Array(classOf[ReactiveSecurityAutoConfiguration], classOf[ReactiveManagementWebSecurityAutoConfiguration]))
@EntityScan(basePackages = Array("edu.ucdavis.fiehnlab.mona.app.server.proxy.domain"))
@EnableJpaRepositories(basePackages = Array("edu.ucdavis.fiehnlab.mona.app.server.proxy"))
@Controller
@RefreshScope
@Import(Array(classOf[EurekaClientConfig]))
class ProxyServer extends LazyLogging {

  @Value("${spring.servlet.multipart.max-file-size}")
  val multipartMaxFileSize: String = null

  @Value("${spring.servlet.multipart.max-request-size}")
  val multipartMaxRequestSize: String = null

  @Bean(name = Array("multipartConfigElement"))
  def multipartConfigElement: MultipartConfigElement = {
    val factory: MultipartConfigFactory = new MultipartConfigFactory()
    factory.setMaxFileSize(DataSize.parse(multipartMaxFileSize))
    factory.setMaxRequestSize(DataSize.parse(multipartMaxRequestSize))
    factory.setLocation(System.getProperty("java.io.tmpdir"))
    factory.createMultipartConfig()
  }

//  @Bean
//  @Lazy(false)
//  def apis(swaggerUiConfigParameters: SwaggerUiConfigParameters, locator: RouteDefinitionLocator): List[GroupedOpenApi] = {
//    val groups: ListBuffer[GroupedOpenApi] = new ListBuffer[GroupedOpenApi]
//    val definitions: Buffer[RouteDefinition] = locator.getRouteDefinitions().collectList().block().asScala
//    for (definition <- definitions) {
//      logger.info(s"id: ${definition.getId} ${definition.getUri.toString}")
//    }
//    definitions.asJava.stream().filter{routeDefinition =>
//      routeDefinition.getId.matches(".*-service")
//    }.forEach{routeDefinition =>
//      val name: String = routeDefinition.getId.replaceAll("-service","")
//      swaggerUiConfigParameters.addGroup(name);
//      GroupedOpenApi.builder().pathsToMatch(s"/${name}/**").group(name).build()
//    }
//    groups.toList
//  }

}

object ProxyServer extends App {
  new SpringApplication(classOf[ProxyServer]).run()
}


  /*
  @Bean
  def rewriteFilter: ZuulFilter = {
    new SwaggerRedirectFilter
  }*/



@Configuration
class CorsConfig extends WebMvcConfigurer with LazyLogging {

  @Bean
  def resources(): Resources = {
    new Resources
  }

  @Autowired
  val resourceProperties: Resources = null

  override def addCorsMappings(registry: CorsRegistry): Unit = registry.addMapping("/**")

  override def addResourceHandlers(registry: ResourceHandlerRegistry): Unit = {

    // Add handler for all static files and REST endpoints
    registry.addResourceHandler(
      // Web resources
      "/**/*.css", "/**/*.html", "/**/*.js", "/**/*.json", "/**/*.jpg",
      "/**/*.jpeg", "/**/*.png", "/**/*.ttf", "/**/*.eot", "/**/*.svg", "/**/*.woff", "/**/*.woff2",


      // API endpoints
      "/rest/**", "/**/v3/api-docs", "/swagger-ui/**", "/swagger-ui.html/**", "/v3/api-docs/**"
    )
      .addResourceLocations(resourceProperties.getStaticLocations: _*)
      .setCachePeriod(300)

    //Will reinstate resourceProperties for setCachePeriod in 2.7

    // Handle all other paths with angular
    registry.addResourceHandler("/**/*")
      .addResourceLocations(resourceProperties.getStaticLocations.map(x => x + "index.html"): _*)
      .setCachePeriod(300)
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
