package edu.ucdavis.fiehnlab.mona.app.node

/**
  * Created by wohlgemuth on 3/17/16.
  */
@SpringBootApplication(scanBasePackages = Array("edu.ucdavis.fiehnlab.mona.backend.core"))
class MonaNode

/**
  * starter
  */
object MonaNode extends App {
  SpringApplication.run(classOf[MonaNode])
}
