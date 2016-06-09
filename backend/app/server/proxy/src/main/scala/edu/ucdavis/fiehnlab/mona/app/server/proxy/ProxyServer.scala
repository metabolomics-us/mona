package edu.ucdavis.fiehnlab.mona.app.server.proxy

import java.net.URL

import com.netflix.zuul.ZuulFilter
import com.netflix.zuul.context.RequestContext
import com.typesafe.scalalogging.LazyLogging
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.SpringApplication
import org.springframework.boot.autoconfigure.SpringBootApplication
import org.springframework.cloud.client.discovery.EnableDiscoveryClient
import org.springframework.cloud.context.config.annotation.RefreshScope
import org.springframework.cloud.netflix.zuul.EnableZuulProxy
import org.springframework.cloud.netflix.zuul.filters.ProxyRouteLocator
import org.springframework.context.annotation.{Bean, Configuration}
import org.springframework.stereotype.{Component, Controller}
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
class ProxyServer

@Configuration
class CorsConfig extends WebMvcConfigurerAdapter {
  override def addCorsMappings(registry: CorsRegistry): Unit = registry.addMapping("/**")

}

@Component
class RewriteFilter extends ZuulFilter with LazyLogging {
  @Autowired
  val routeLocator: ProxyRouteLocator = null


  override def filterType(): String = "pre"

  override def filterOrder(): Int = 100

  override def shouldFilter(): Boolean = {
    val request = RequestContext.getCurrentContext
    request.get("requestURI") != null && request.get("requestURI").toString.startsWith("/documentation")

  }

  override def run(): AnyRef = {

    val request = RequestContext.getCurrentContext
    val uri = request.get("requestURI").toString

    logger.info("need to redirect")

    //HERE WE NEED TO REWRITE IT SOMEHOW
    
    null
  }
}

object ProxyServer extends App {

  // System.setProperty("spring.config.name", "proxy-service");

  new SpringApplication(classOf[ProxyServer]).run()

}
