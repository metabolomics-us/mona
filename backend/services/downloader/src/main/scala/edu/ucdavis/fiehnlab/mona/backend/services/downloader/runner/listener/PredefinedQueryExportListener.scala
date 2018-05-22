package edu.ucdavis.fiehnlab.mona.backend.services.downloader.runner.listener

import com.typesafe.scalalogging.LazyLogging
import edu.ucdavis.fiehnlab.mona.backend.core.amqp.event.listener.GenericMessageListener
import edu.ucdavis.fiehnlab.mona.backend.services.downloader.core.repository.PredefinedQueryMongoRepository
import edu.ucdavis.fiehnlab.mona.backend.services.downloader.core.types.PredefinedQuery
import edu.ucdavis.fiehnlab.mona.backend.services.downloader.runner.service.DownloaderService
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.stereotype.Component

/**
  * Created by sajjan on 6/9/16.
  */
@Component
class PredefinedQueryExportListener extends GenericMessageListener[PredefinedQuery] with LazyLogging {

  @Autowired
  val downloadService: DownloaderService = null

  @Autowired
  val predefinedQueryRepository: PredefinedQueryMongoRepository = null


  override def handleMessage(predefinedQuery: PredefinedQuery): Unit = {
    try {
      logger.info(s"Received predefined query download request: ${predefinedQuery.label}")

      // Download query
      val result: PredefinedQuery = downloadService.downloadPredefinedQuery(predefinedQuery)
      predefinedQueryRepository.save(result)

      logger.info(s"Finished downloading predefined querry ${result.label}, exported ${result.jsonExport.count} spectra")
    } catch {
      case e: Exception => logger.error(s"Error during download of predefined query ${predefinedQuery.label}, failing silently: ${e.getMessage}", e)
    }
  }
}
