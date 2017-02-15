package edu.ucdavis.fiehnlab.mona.backend.bootstrap.service

import java.util
import com.typesafe.scalalogging.LazyLogging
import edu.ucdavis.fiehnlab.mona.backend.core.domain.Spectrum
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

    val it: util.Iterator[Spectrum] = spectrumPersistenceService.findAll().iterator()
    var counter: Int = 0

    while(it.hasNext) {
      val spectrum: Spectrum = it.next()
      spectrumPersistenceService.fireSyncEvent(spectrum)
      counter += 1

      if (counter % 10000 == 0) {
        logger.info(s"\tSynchronized spectrum #$counter with id ${spectrum.id}")
      } else {
        logger.trace(s"\tSynchronized spectrum #$counter with id ${spectrum.id}")
      }
    }

    spectrumPersistenceService.forceSynchronization()
    logger.info(s"Synchronization of $counter spectra finished in ${(System.currentTimeMillis() - startTime) / 1000} seconds")
  }
}
