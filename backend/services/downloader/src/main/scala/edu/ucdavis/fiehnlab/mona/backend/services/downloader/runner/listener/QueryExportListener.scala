package edu.ucdavis.fiehnlab.mona.backend.services.downloader.runner.listener

import com.typesafe.scalalogging.LazyLogging
import edu.ucdavis.fiehnlab.mona.backend.core.amqp.event.listener.GenericMessageListener
import edu.ucdavis.fiehnlab.mona.backend.services.downloader.core.repository.{PredefinedQueryRepository, QueryExportRepository}
import edu.ucdavis.fiehnlab.mona.backend.services.downloader.domain.QueryExport
import edu.ucdavis.fiehnlab.mona.backend.services.downloader.runner.service.DownloaderService
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.context.annotation.Profile
import org.springframework.stereotype.Component

/**
  * Created by sajjan on 6/9/16.
  */
@Component
@Profile(Array("mona.persistence.downloader"))
class QueryExportListener extends GenericMessageListener[QueryExport] with LazyLogging {

  @Autowired
  val downloadService: DownloaderService = null

  @Autowired
  val queryExportRepository: QueryExportRepository = null

  @Autowired
  val predefinedQueryRepository: PredefinedQueryRepository = null


  override def handleMessage(export: QueryExport): Unit = {
    try {
      logger.info(s"Received download request: ${export.getLabel}")

      if (export.getFormat.equalsIgnoreCase("static")) {
        // Export static formats for the given query
        val result: QueryExport = downloadService.generateStaticExports(export)

        logger.info(s"Finished exporting static downloads for ${result.getLabel}, exported ${result.getCount} spectra")
      } else {
        // Export query
        val result: QueryExport = downloadService.generateQueryExport(export)

        // Save updated query export for non-static exports
        queryExportRepository.save(result)

        // Update predefined download if necessary
        if (result.getEmailAddress == null || result.getEmailAddress.isEmpty) {
          val predefinedQuery = predefinedQueryRepository.findById(result.getLabel).get()

          if (predefinedQuery != null) {
            // Update the format-specific export in the predefined query
            result.getFormat match {
              case "json" => {
                predefinedQuery.setJsonExport(result)
                predefinedQuery.setQueryCount(result.getCount)
                predefinedQueryRepository.save(predefinedQuery)
              }
              case "msp" => {
                predefinedQuery.setMspExport(result)
                predefinedQuery.setQueryCount(result.getCount)
                predefinedQueryRepository.save(predefinedQuery)
              }
              case "sdf" => {
                predefinedQuery.setSdfExport(result)
                predefinedQuery.setQueryCount(result.getCount)
                predefinedQueryRepository.save(predefinedQuery)
              }
            }
          }
        }

        logger.info(s"Finished exporting ${result.getCount} spectra for ${result.getLabel}")
      }
    } catch {
      case e: Exception => logger.error(s"exception during download of ${export.getLabel}, failing silently: ${e.getMessage}", e)
    }
  }
}
