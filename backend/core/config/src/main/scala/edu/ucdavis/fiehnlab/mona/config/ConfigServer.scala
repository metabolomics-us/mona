package edu.ucdavis.fiehnlab.mona.config

import org.springframework.boot.SpringApplication
import org.springframework.boot.autoconfigure.SpringBootApplication
import org.springframework.cloud.config.server.EnableConfigServer

@EnableConfigServer
@SpringBootApplication
class ConfigServer

object ConfigServer extends App {
  new SpringApplication(classOf[ConfigServer]).run()
}
