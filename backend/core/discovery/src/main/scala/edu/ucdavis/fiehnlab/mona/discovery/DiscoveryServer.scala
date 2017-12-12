package edu.ucdavis.fiehnlab.mona.discovery

import org.springframework.boot.SpringApplication
import org.springframework.boot.autoconfigure.SpringBootApplication
import org.springframework.cloud.config.server.EnableConfigServer
import org.springframework.cloud.netflix.eureka.server.EnableEurekaServer

/**
  * Created by wohlgemuth on 3/28/16.
  */
@EnableEurekaServer
@EnableConfigServer
@SpringBootApplication
class DiscoveryServer

object DiscoveryServer extends App {
  new SpringApplication(classOf[DiscoveryServer]).run()
}