package edu.ucdavis.fiehnlab.mona.backend.bootstrap.service

import com.typesafe.scalalogging.LazyLogging
import edu.ucdavis.fiehnlab.mona.backend.core.persistence.service.persistence.SpectrumPersistenceService
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.stereotype.Service

/**
  * Created by sajjan on 7/14/16.
  */
@Service
class BootstrapPersistenceService extends LazyLogging {

  @Autowired
  val spectrumPersistenceService: SpectrumPersistenceService = null

  def synchronizeDatabases(): Unit = {
    logger.info("Starting synchronization between MongoDB and ElasticSearch...")
    val startTime: Long = System.currentTimeMillis()

    spectrumPersistenceService.forceSynchronization()
    logger.info(s"Synchronization finished in ${(System.currentTimeMillis() - startTime) / 1000} seconds")
  }
}
