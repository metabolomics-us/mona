package edu.ucdavis.fiehnlab.mona.app.server.config

import org.springframework.boot.SpringApplication
import org.springframework.boot.autoconfigure.SpringBootApplication
import org.springframework.cloud.client.discovery.EnableDiscoveryClient
import org.springframework.cloud.config.server.EnableConfigServer

/**
  * Created by wohlgemuth on 3/28/16.
  */
@SpringBootApplication
@EnableConfigServer
@EnableDiscoveryClient
class ConfigServer {

}

object ConfigServer extends App{

  System.setProperty("spring.config.name", "config-server");
  new SpringApplication(classOf[ConfigServer]).run()
}