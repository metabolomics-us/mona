package edu.ucdavis.fiehnlab.mona.app.server.config

import org.springframework.boot.SpringApplication
import org.springframework.boot.autoconfigure.SpringBootApplication
import org.springframework.cloud.client.discovery.EnableDiscoveryClient
import org.springframework.cloud.config.server.EnableConfigServer
import org.springframework.cloud.context.config.annotation.RefreshScope

/**
  * Created by wohlgemuth on 3/28/16.
  */
@SpringBootApplication
@EnableConfigServer
@EnableDiscoveryClient
@RefreshScope
class ConfigServer {

}

object ConfigServer extends App {
  new SpringApplication(classOf[ConfigServer]).run()
}