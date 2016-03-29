package edu.ucdavis.fiehnlab.mona.app.server.proxy

import org.springframework.boot.SpringApplication
import org.springframework.boot.autoconfigure.SpringBootApplication
import org.springframework.cloud.client.discovery.EnableDiscoveryClient
import org.springframework.cloud.netflix.zuul.EnableZuulProxy
import org.springframework.stereotype.Controller

/**
  * Created by wohlgemuth on 3/28/16.
  */
@SpringBootApplication
@EnableDiscoveryClient
@EnableZuulProxy
@Controller
class ProxyServer {

}

object ProxyServer extends App{

  System.setProperty("spring.config.name", "edge-server");

  new SpringApplication(classOf[ProxyServer]).run()

}
