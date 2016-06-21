package edu.ucdavis.fiehnlab.mona.backend.bootstrap

import com.typesafe.scalalogging.LazyLogging
import edu.ucdavis.fiehnlab.mona.backend.bootstrap.service.BootstrapDownloaderService
import edu.ucdavis.fiehnlab.mona.backend.services.downloader.config.DownloadConfig
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.{ApplicationArguments, ApplicationRunner, SpringApplication}
import org.springframework.boot.autoconfigure.SpringBootApplication
import org.springframework.context.annotation.Import

/**
  * Created by sajjan on 6/16/16.
  */
@SpringBootApplication
@Import(Array(classOf[DownloadConfig]))
class Bootstrap extends ApplicationRunner with LazyLogging {

  @Autowired
  val downloaderService: BootstrapDownloaderService = null

  override def run(applicationArguments: ApplicationArguments): Unit = {
    // Configure downloader
    downloaderService.createPredefinedQueries()
  }
}

object Bootstrap extends App {
  val app = new SpringApplication(classOf[Bootstrap])
  app.setWebEnvironment(false)

  val context = app.run(args: _*)
}