package edu.ucdavis.fiehnlab.mona.backend.services.repository

import org.springframework.boot.SpringApplication
import org.springframework.boot.autoconfigure.SpringBootApplication


@SpringBootApplication()
class RepositoryRunner {

}

/**
  * Created by sajjan on 5/19/16.
  */
object RepositoryRunner extends App {
  new SpringApplication(classOf[RepositoryRunner]).run()
}