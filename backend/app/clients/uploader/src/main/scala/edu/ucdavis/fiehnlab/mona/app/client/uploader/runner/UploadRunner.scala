package edu.ucdavis.fiehnlab.mona.app.client.uploader.runner

import com.typesafe.scalalogging.LazyLogging
import edu.ucdavis.fiehnlab.mona.backend.core.domain.HelperTypes.LoginRequest
import edu.ucdavis.fiehnlab.mona.backend.core.domain.service.LoginService
import org.springframework.batch.core.launch.JobLauncher
import org.springframework.batch.core.{Job, JobParametersBuilder}
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.{ApplicationArguments, ApplicationRunner}
import org.springframework.stereotype.Component

import scala.collection.JavaConverters._

/**
  * this utilizes our defined jobs and runs them against the specified server
  */
@Component
class UploadRunner extends ApplicationRunner with LazyLogging {

  @Autowired
  val jobLauncher: JobLauncher = null

  @Autowired
  val uploadSpectraJob: Job = null

  @Autowired
  val uploadLegacySpectraJob: Job = null

  @Autowired
  val loginService: LoginService = null

  /**
    * runs the actual application and populates our jobs
    *
    * @param applicationArguments
    */
  override def run(applicationArguments: ApplicationArguments): Unit = {

    if (applicationArguments.containsOption("username") && applicationArguments.containsOption("password")) {
      val token = loginService.login(LoginRequest(applicationArguments.getOptionValues("username").get(0), applicationArguments.getOptionValues("password").get(0))).token

      readFile(token, applicationArguments)
    }
    else if (applicationArguments.containsOption("token")) {
      readFile(applicationArguments.getOptionValues("token").get(0), applicationArguments)
    }
    else {
      usage
    }
  }

  def usage: Unit = {
    println()
    println("Usage: ")
    println("")
    println("\t --file=name\t\t\tthe json file you would like to upload")
    println("")
    println("Authentication: ")
    println("")
    println("Currently the upload supports token authentication as well as username/password login, for token based please use")
    println("")
    println("\t --token=TOKEN\t\t\tyour previously generated token")
    println("")
    println("for username and password based authentication please us")
    println("")
    println("\t --username=USER")
    println("\t --password=PASSWORD")
    println("")
    println("Optional: ")
    println("")
    println("\t --mona.rest.server.host=127.0.0.1\t\t to specify which server to use")
    println("\t --mona.rest.server.port=8080\t\t to specify which port to use")
    println("\t --legacy\t\t utilize the old MoNA format for input")


    System.exit(-1)
  }

  /**
    * reads the file and does the actual authorization
    *
    * @param token
    * @param applicationArguments
    */
  def readFile(token: String, applicationArguments: ApplicationArguments) = {

    if (applicationArguments.containsOption("file")) {
      applicationArguments.getOptionValues("file").asScala.foreach { file =>
        logger.info(s"reading file: ${file}")

        try {
          val parameters = new JobParametersBuilder().addString("pathToFile", file).addString("loginToken", token).toJobParameters

          if (applicationArguments.containsOption("legacy")) {
            logger.debug("running legacy import mode")
            jobLauncher.run(uploadLegacySpectraJob, parameters)
          }
          else {
            jobLauncher.run(uploadSpectraJob, parameters)
          }

          System.exit(1)

        }
        catch {

          case e: Exception =>
            logger.error(s"received error: ${e.getMessage}", e)
            System.exit(-2)

        }
      }
    }
    else {
      usage
    }
  }
}
