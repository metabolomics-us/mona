package edu.ucdavis.fiehnlab.mona.app.client.uploader

import org.springframework.boot.SpringApplication
import org.springframework.boot.autoconfigure.SpringBootApplication
import org.springframework.boot.autoconfigure.data.elasticsearch.{ElasticsearchAutoConfiguration, ElasticsearchDataAutoConfiguration}

/**
  * Created by wohlg on 3/19/2016.
  */

@SpringBootApplication(exclude = Array(classOf[ElasticsearchDataAutoConfiguration], classOf[ElasticsearchAutoConfiguration]))
class Uploader {

}

/**
  * is utilized to easily batch upload existing data to the mona server
  */
object Uploader extends App {
  val app = new SpringApplication(classOf[Uploader])
  app.setWebEnvironment(false)

  val context = app.run(args: _*)
}
