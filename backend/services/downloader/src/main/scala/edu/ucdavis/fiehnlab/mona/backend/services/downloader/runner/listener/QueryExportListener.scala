package edu.ucdavis.fiehnlab.mona.backend.services.downloader.runner.listener

import com.typesafe.scalalogging.LazyLogging
import edu.ucdavis.fiehnlab.mona.backend.core.amqp.event.listener.GenericMessageListener
import edu.ucdavis.fiehnlab.mona.backend.services.downloader.core.repository.{PredefinedQueryMongoRepository, QueryExportMongoRepository}
import edu.ucdavis.fiehnlab.mona.backend.services.downloader.core.types.QueryExport
import edu.ucdavis.fiehnlab.mona.backend.services.downloader.runner.service.DownloaderService
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.stereotype.Component

/**
  * Created by sajjan on 6/9/16.
  */
@Component
class QueryExportListener extends GenericMessageListener[QueryExport] with LazyLogging {

  @Autowired
  val downloadService: DownloaderService = null

  @Autowired
  val queryExportRepository: QueryExportMongoRepository = null

  @Autowired
  val predefinedQueryRepository: PredefinedQueryMongoRepository = null


  override def handleMessage(export: QueryExport): Unit = {
    try {
      logger.info(s"Received download request: ${export.label}")

      if (export.format.equalsIgnoreCase("static")) {
        // Export static formats for the given query
        val result: QueryExport = downloadService.generateStaticExports(export)

        logger.info(s"Finished exporting static downloads for ${result.label}, exported ${result.count} spectra")
      } else {
        // Export query
        val result: QueryExport = downloadService.generateQueryExport(export)

        // Save updated query export for non-static exports
        queryExportRepository.save(result)

        // Update predefined download if necessary
        if (result.emailAddress == null || result.emailAddress.isEmpty) {
          val predefinedQuery = predefinedQueryRepository.findById(result.label).get()

          if (predefinedQuery != null) {
            // Update the format-specific export in the predefined query
            result.format match {
              case "json" => predefinedQueryRepository.save(predefinedQuery.copy(jsonExport = result, queryCount = result.count))
              case "msp" => predefinedQueryRepository.save(predefinedQuery.copy(mspExport = result, queryCount = result.count))
              case "sdf" => predefinedQueryRepository.save(predefinedQuery.copy(sdfExport = result, queryCount = result.count))
            }
          }
        }

        logger.info(s"Finished exporting ${result.count} spectra for ${result.label}")
      }
    } catch {
      case e: Exception => logger.error(s"exception during download of ${export.label}, failing silently: ${e.getMessage}", e)
    }
  }
}
