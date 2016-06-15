package edu.ucdavis.fiehnlab.mona.app.server.proxy

import com.netflix.zuul.ZuulFilter
import com.typesafe.scalalogging.LazyLogging
import edu.ucdavis.fiehnlab.mona.app.server.proxy.documentation.SwaggerRedirectFilter
import edu.ucdavis.fiehnlab.mona.backend.core.persistence.rest.CustomErrorController
import org.springframework.boot.SpringApplication
import org.springframework.boot.autoconfigure.SpringBootApplication
import org.springframework.cloud.client.discovery.EnableDiscoveryClient
import org.springframework.cloud.context.config.annotation.RefreshScope
import org.springframework.cloud.netflix.zuul.EnableZuulProxy
import org.springframework.context.annotation.{Bean, ComponentScan, Configuration}
import org.springframework.core.annotation.Order
import org.springframework.security.config.annotation.web.builders.WebSecurity
import org.springframework.security.config.annotation.web.configuration.WebSecurityConfigurerAdapter
import org.springframework.stereotype.Controller
import org.springframework.web.servlet.config.annotation.{CorsRegistry, WebMvcConfigurerAdapter}
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
class ProxyServer{

  @Bean
  def rewriteFilter: ZuulFilter = {
    new SwaggerRedirectFilter()
  }
}

@Configuration
class CorsConfig extends WebMvcConfigurerAdapter with LazyLogging {
  override def addCorsMappings(registry: CorsRegistry): Unit = registry.addMapping("/**")
}

@Configuration
@Order(132)
@ComponentScan(basePackageClasses = Array(classOf[CustomErrorController]))
class Security extends WebSecurityConfigurerAdapter {

  override def configure(web: WebSecurity) {
    web.ignoring.anyRequest()
  }
}


object ProxyServer extends App {

  // System.setProperty("spring.config.name", "proxy-service");

  new SpringApplication(classOf[ProxyServer]).run()

}
