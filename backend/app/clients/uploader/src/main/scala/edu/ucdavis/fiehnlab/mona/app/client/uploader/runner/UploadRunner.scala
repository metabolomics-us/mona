package edu.ucdavis.fiehnlab.mona.app.client.uploader.runner

import java.io.File

import com.typesafe.scalalogging.LazyLogging
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
  val uploadAndCurrationSpectraJob: Job = null

  /**
    * runs the actual application and populates our jobs
    *
    * @param applicationArguments
    */
  override def run(applicationArguments: ApplicationArguments): Unit = {

    if (applicationArguments.containsOption("file")) {
      applicationArguments.getOptionValues("file").asScala.foreach { file =>
        logger.info(s"reading file: ${file}")

        val parameters = new JobParametersBuilder().addString("pathToFile", file).toJobParameters

        if (applicationArguments.containsOption("curate")) {
          jobLauncher.run(uploadAndCurrationSpectraJob, parameters)
        }
        else {
          jobLauncher.run(uploadSpectraJob, parameters)
        }
      }
    }
    else {
      println()
      println("Usage: ")
      println("")
      println("\t --file=name\t\t\tthe json file you would like to upload")
      println("")
      println("Optional: ")
      println("")
      println("\t --curate\t\t jobs are not only uploaded, but also curated at the same time")


      System.exit(-1)

    }
  }

}
