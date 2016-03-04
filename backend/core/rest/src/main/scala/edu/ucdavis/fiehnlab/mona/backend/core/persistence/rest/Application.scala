package edu.ucdavis.fiehnlab.mona.backend.core.persistence.rest

import edu.ucdavis.fiehnlab.mona.backend.core.persistence.mongo.config.MongoConfig
import edu.ucdavis.fiehnlab.mona.backend.core.persistence.rest.config.RESTConfig
import org.springframework.boot.SpringApplication
import org.springframework.boot.autoconfigure.EnableAutoConfiguration
import org.springframework.context.annotation.{Import, ComponentScan}

/**
  * Created by wohlgemuth on 2/29/16.
  */
@ComponentScan
@EnableAutoConfiguration
@Import(Array(classOf[MongoConfig], classOf[RESTConfig]))
class Application

object Application {

  def main(args: Array[String]): Unit = {

    val app = new SpringApplication(classOf[Application])
    app.run(args: _*)
  }

}

