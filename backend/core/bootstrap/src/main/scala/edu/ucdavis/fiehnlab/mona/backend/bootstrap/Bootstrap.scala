package edu.ucdavis.fiehnlab.mona.backend.bootstrap

import com.typesafe.scalalogging.LazyLogging
import edu.ucdavis.fiehnlab.mona.backend.bootstrap.service.{BootstrapDownloaderService, BootstrapPersistenceService}
import edu.ucdavis.fiehnlab.mona.backend.core.persistence.rest.SwaggerConfig
import edu.ucdavis.fiehnlab.mona.backend.core.persistence.service.config.PersistenceServiceConfig
import edu.ucdavis.fiehnlab.mona.backend.services.downloader.core.config.DownloadConfig
import edu.ucdavis.fiehnlab.mona.backend.services.downloader.core.repository.{PredefinedQueryMongoRepository, QueryExportMongoRepository}
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.{ApplicationArguments, ApplicationRunner, SpringApplication}
import org.springframework.boot.autoconfigure.SpringBootApplication
import org.springframework.cloud.client.discovery.EnableDiscoveryClient
import org.springframework.context.annotation.Import
import org.springframework.data.mongodb.repository.config.EnableMongoRepositories
import springfox.documentation.swagger2.annotations.EnableSwagger2

/**
  * Created by sajjan on 6/16/16.
  */
@SpringBootApplication
@Import(Array(classOf[PersistenceServiceConfig]))
@EnableMongoRepositories(basePackageClasses = Array(classOf[QueryExportMongoRepository], classOf[PredefinedQueryMongoRepository]))
class Bootstrap extends ApplicationRunner with LazyLogging {

  @Autowired
  val downloaderService: BootstrapDownloaderService = null

  @Autowired
  val persistenceService: BootstrapPersistenceService = null


  override def run(applicationArguments: ApplicationArguments): Unit = {
    // Configure downloader
    downloaderService.createPredefinedQueries()

    // Synchronize ElasticSearch index
    persistenceService.synchronizeDatabases()
  }
}

object Bootstrap extends App {
  val app = new SpringApplication(classOf[Bootstrap])
  app.setWebEnvironment(false)

  System.exit(SpringApplication.exit(app.run(args: _*)))
}