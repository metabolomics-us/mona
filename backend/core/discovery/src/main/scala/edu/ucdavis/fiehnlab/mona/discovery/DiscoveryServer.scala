package edu.ucdavis.fiehnlab.mona.discovery

import org.springframework.boot.SpringApplication
import org.springframework.boot.autoconfigure.SpringBootApplication
import org.springframework.cloud.netflix.eureka.server.EnableEurekaServer

/**
  * Created by wohlgemuth on 3/28/16.
  */
@EnableEurekaServer
@SpringBootApplication
class DiscoveryServer

object DiscoveryServer extends App {
  new SpringApplication(classOf[DiscoveryServer]).run()
}
