package edu.ucdavis.fiehnlab.mona.app.node

import org.springframework.boot.SpringApplication
import org.springframework.boot.autoconfigure.SpringBootApplication

/**
  * Created by wohlgemuth on 3/17/16.
  */
@SpringBootApplication(scanBasePackages = Array("edu.ucdavis.fiehnlab.mona.backend.core"))
class MonaNode

/**
  * starter
  */
object MonaNode extends App {
  SpringApplication.run(classOf[MonaNode]);
}
