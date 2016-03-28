package edu.ucdavis.fiehnlab.mona.app.server.discovery

import org.springframework.boot.SpringApplication
import org.springframework.boot.autoconfigure.SpringBootApplication
import org.springframework.cloud.netflix.eureka.server.EnableEurekaServer

/**
  * Created by wohlgemuth on 3/28/16.
  */
@SpringBootApplication
@EnableEurekaServer
class DiscoveryServer {

}

object DiscoveryServer extends App {
  System.setProperty("spring.config.name", "registration-server");
  new SpringApplication(classOf[DiscoveryServer]).run()
}