package edu.ucdavis.fiehnlab.mona.app.node

/**
  * Created by wohlgemuth on 3/17/16.
  */

@SpringBootApplication
class MonaMemoryNode extends LazyLogging {


}

object MonaMemoryNode extends App {
  SpringApplication.run(classOf[MonaMemoryNode])
}
