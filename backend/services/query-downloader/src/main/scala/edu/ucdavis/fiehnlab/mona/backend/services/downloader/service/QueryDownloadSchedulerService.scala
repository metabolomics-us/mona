package edu.ucdavis.fiehnlab.mona.backend.services.downloader.service

import edu.ucdavis.fiehnlab.mona.backend.services.downloader.repository.IQueryExportMongoRepository
import org.springframework.beans.factory.annotation.{Qualifier, Autowired}
import org.springframework.stereotype.Service

/**
  * Created by sajjan on 5/24/16.
  */

@Service
class QueryDownloadSchedulerService {
//  @Autowired
//  @Qualifier("spectra-query-download-queue")
//  val queryDownloadQueue: String = null

  @Autowired
  val queryExportMongoRepository: IQueryExportMongoRepository = null

  def scheduleDownload(query: String) = {

  }
}
