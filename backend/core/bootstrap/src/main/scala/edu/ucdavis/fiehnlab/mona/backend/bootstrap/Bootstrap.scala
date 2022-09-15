package edu.ucdavis.fiehnlab.mona.backend.bootstrap

import com.typesafe.scalalogging.LazyLogging
import edu.ucdavis.fiehnlab.mona.backend.bootstrap.service.BootstrapDownloaderService
import edu.ucdavis.fiehnlab.mona.backend.core.persistence.postgresql.config.PostgresqlConfiguration
import edu.ucdavis.fiehnlab.mona.backend.services.downloader.core.repository.{PredefinedQueryRepository, QueryExportRepository}
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.autoconfigure.SpringBootApplication
import org.springframework.boot.{ApplicationArguments, ApplicationRunner, SpringApplication, WebApplicationType}
import org.springframework.context.annotation.Import
import org.springframework.data.jpa.repository.config.EnableJpaRepositories

/**
  * Created by sajjan on 6/16/16.
  */
@SpringBootApplication
@Import(Array(classOf[PostgresqlConfiguration]))
@EnableJpaRepositories(basePackageClasses = Array(classOf[QueryExportRepository], classOf[PredefinedQueryRepository]))
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
  app.setWebApplicationType(WebApplicationType.NONE)

  System.exit(SpringApplication.exit(app.run(args: _*)))
}
