package edu.ucdavis.fiehnlab.mona.backend.services.downloader.runner.listener

import java.util.Optional

import com.typesafe.scalalogging.LazyLogging
import edu.ucdavis.fiehnlab.mona.backend.core.amqp.event.listener.GenericMessageListener
import edu.ucdavis.fiehnlab.mona.backend.services.downloader.core.repository.{PredefinedQueryMongoRepository, QueryExportMongoRepository}
import edu.ucdavis.fiehnlab.mona.backend.services.downloader.core.types.{PredefinedQuery, QueryExport}
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

      // Download query
      val result: QueryExport = downloadService.downloadQueryExport(export)

      // Save updated query export
      queryExportRepository.save(result)

      // Update predefined download if necessary
      if (result.emailAddress == null || result.emailAddress.isEmpty) {
        val predefinedQuery: Optional[PredefinedQuery] = predefinedQueryRepository.findById(result.label)

        if (predefinedQuery.isPresent) {
          // Update jsonExport or mspExport in the predefined query
          val updatedPredefinedQuery: PredefinedQuery =
            if (result.format == "json")
              predefinedQuery.get().copy(jsonExport = result, queryCount = result.count)
            else if (result.format == "msp")
              predefinedQuery.get().copy(mspExport = result, queryCount = result.count)
            else if (result.format == "sdf")
              predefinedQuery.get().copy(sdfExport = result, queryCount = result.count)
            else
              predefinedQuery.get()

          // Save the updated predefined query
          predefinedQueryRepository.save(updatedPredefinedQuery)
        }
      }

      logger.info(s"Finished downloading ${result.label}, exported ${result.count} spectra")

    } catch {
      case e: Exception => logger.error(s"exception during download of ${export.label}, failing silently: ${e.getMessage}", e)
    }
  }
}
