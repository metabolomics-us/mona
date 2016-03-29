package edu.ucdavis.fiehnlab.mona.app.node

import com.typesafe.scalalogging.LazyLogging
import org.springframework.boot.SpringApplication
import org.springframework.boot.autoconfigure.SpringBootApplication

/**
  * Created by wohlgemuth on 3/17/16.
  */

@SpringBootApplication
class MonaMemoryNode extends LazyLogging{


}

object MonaMemoryNode extends App {
  SpringApplication.run(classOf[MonaMemoryNode])
}
