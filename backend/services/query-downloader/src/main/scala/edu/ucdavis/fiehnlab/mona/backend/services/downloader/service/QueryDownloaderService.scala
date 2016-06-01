package edu.ucdavis.fiehnlab.mona.backend.services.downloader.service

import org.springframework.beans.factory.annotation.{Qualifier, Autowired}
import org.springframework.stereotype.Service

/**
  * Created by sajjan on 5/24/16.
  */
@Service
class QueryDownloaderService {
  @Autowired
  @Qualifier("spectra-query-download-queue")
  val queueName: String = null
}
