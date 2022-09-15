package edu.ucdavis.fiehnlab.mona.app.client.uploader

import org.springframework.boot.{SpringApplication, WebApplicationType}
import org.springframework.boot.autoconfigure.SpringBootApplication

/**
  * Created by wohlg on 3/19/2016.
  */

@SpringBootApplication()
class Uploader {

}

/**
  * is utilized to easily batch upload existing data to the mona server
  */
object Uploader extends App {
  val app = new SpringApplication(classOf[Uploader])
  app.setWebApplicationType(WebApplicationType.NONE)

  val context = app.run(args: _*)
}
