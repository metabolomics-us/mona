package edu.ucdavis.fiehnlab.mona.app.server.proxy

import com.netflix.zuul.ZuulFilter
import com.typesafe.scalalogging.LazyLogging
import edu.ucdavis.fiehnlab.mona.app.server.proxy.documentation.SwaggerRedirectFilter
import org.springframework.boot.SpringApplication
import org.springframework.boot.autoconfigure.SpringBootApplication
import org.springframework.cloud.client.discovery.EnableDiscoveryClient
import org.springframework.cloud.context.config.annotation.RefreshScope
import org.springframework.cloud.netflix.zuul.EnableZuulProxy
import org.springframework.context.annotation.{Bean, Configuration}
import org.springframework.stereotype.Controller
import org.springframework.web.servlet.config.annotation.{CorsRegistry, WebMvcConfigurerAdapter}

/**
  * Created by wohlgemuth on 3/28/16.
  */
@SpringBootApplication
@EnableDiscoveryClient
@EnableZuulProxy
@Controller
@RefreshScope
class ProxyServer {

  @Bean
  def rewriteFilter: ZuulFilter = {
    new SwaggerRedirectFilter()
  }
}

@Configuration
class CorsConfig extends WebMvcConfigurerAdapter with LazyLogging {
  override def addCorsMappings(registry: CorsRegistry): Unit = registry.addMapping("/**")
}


object ProxyServer extends App {
  // System.setProperty("spring.config.name", "proxy-service");

  new SpringApplication(classOf[ProxyServer]).run()
}
