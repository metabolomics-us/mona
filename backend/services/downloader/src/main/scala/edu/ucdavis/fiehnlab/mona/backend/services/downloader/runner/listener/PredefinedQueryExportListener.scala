package edu.ucdavis.fiehnlab.mona.backend.services.downloader.runner.listener

import com.typesafe.scalalogging.LazyLogging
import edu.ucdavis.fiehnlab.mona.backend.core.amqp.event.listener.GenericMessageListener
import edu.ucdavis.fiehnlab.mona.backend.services.downloader.core.repository.{PredefinedQueryRepository, QueryExportRepository}
import edu.ucdavis.fiehnlab.mona.backend.services.downloader.domain.PredefinedQuery
import edu.ucdavis.fiehnlab.mona.backend.services.downloader.runner.service.DownloaderService
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.context.annotation.Profile
import org.springframework.stereotype.Component

/**
  * Created by sajjan on 5/18/18.
  */
@Component
@Profile(Array("mona.persistence.downloader"))
class PredefinedQueryExportListener extends GenericMessageListener[PredefinedQuery] with LazyLogging {

  @Autowired
  val downloadService: DownloaderService = null

  @Autowired
  val queryExportRepository: QueryExportRepository = null

  @Autowired
  val predefinedQueryRepository: PredefinedQueryRepository = null


  override def handleMessage(predefinedQuery: PredefinedQuery): Unit = {
    try {
      logger.info(s"Received predefined query download request: ${predefinedQuery.getLabel}")

      // Download query
      val result: PredefinedQuery = downloadService.generatePredefinedExport(predefinedQuery)

      queryExportRepository.save(result.getJsonExport)
      queryExportRepository.save(result.getMspExport)
      queryExportRepository.save(result.getSdfExport)
      predefinedQueryRepository.save(result)

      logger.info(s"Finished downloading predefined querry ${result.getLabel}, exported ${result.getJsonExport.getCount} spectra")
    } catch {
      case e: Exception => logger.error(s"Error during download of predefined query ${predefinedQuery.getLabel}, failing silently: ${e.getMessage}", e)
    }
  }
}
