package edu.ucdavis.fiehnlab.mona.app.client.uploader.runner

import com.typesafe.scalalogging.LazyLogging
import org.springframework.batch.core.launch.JobLauncher
import org.springframework.batch.core.{Job, JobParametersBuilder}
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.{ApplicationArguments, ApplicationRunner}
import org.springframework.stereotype.Component

import scala.collection.JavaConverters._

/**
  * Created by wohlgemuth on 3/21/16.
  */
@Component
class UploadRunner extends ApplicationRunner with LazyLogging{

  @Autowired
  val jobLauncher:JobLauncher = null

  @Autowired
  val uploadSpectraJob:Job = null

  /**
    * runs the actual application and populates our jobs
    *
    * @param applicationArguments
    */
  override def run(applicationArguments: ApplicationArguments): Unit  = {

    if(applicationArguments.containsOption("file")){
      applicationArguments.getOptionValues("file").asScala.foreach { file =>
        logger.info(s"reading file: ${file}")

        val parameters = new JobParametersBuilder().addString("pathToFile",file).toJobParameters

        jobLauncher.run(uploadSpectraJob,parameters)

      }
    }
    else{
      println()
      println("Usage: ")
      println("")
      println("\t --file=name\t\t\tthe json file you would like to upload")
      println("")
      System.exit(-1)

    }
  }

}
