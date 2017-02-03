package edu.ucdavis.fiehnlab.mona.core.similarity.service

import com.typesafe.scalalogging.LazyLogging
import edu.ucdavis.fiehnlab.mona.backend.core.domain.Spectrum
import edu.ucdavis.fiehnlab.mona.backend.core.domain.util.DynamicIterable
import edu.ucdavis.fiehnlab.mona.backend.core.persistence.mongo.repository.ISpectrumMongoRepositoryCustom
import edu.ucdavis.fiehnlab.mona.core.similarity.types.SimpleSpectrum
import edu.ucdavis.fiehnlab.mona.core.similarity.util.IndexUtils
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.context.event.ApplicationReadyEvent
import org.springframework.context.ApplicationListener
import org.springframework.data.domain.{Page, Pageable}
import org.springframework.stereotype.Service

/**
  * Created by sajjan on 1/3/17.
  */
@Service
class SimilarityServiceStartup extends ApplicationListener[ApplicationReadyEvent] with LazyLogging {

  @Autowired
  val spectrumMongoRepository: ISpectrumMongoRepositoryCustom = null

  @Autowired
  val indexUtils: IndexUtils = null

  override def onApplicationEvent(e: ApplicationReadyEvent): Unit = {

    logger.info("Populating indices...")

    val it = new DynamicIterable[Spectrum, String](null, 10) {
      override def fetchMoreData(query: String, pageable: Pageable): Page[Spectrum] = {
          spectrumMongoRepository.findAll(pageable)
      }
    }.iterator

    var counter: Int = 0

    while(it.hasNext) {
      val spectrum: Spectrum = it.next()
      val indexSize: Int = indexUtils.addToIndex(new SimpleSpectrum(spectrum.id, spectrum.spectrum))

      counter += 1

      if (counter % 10000 == 0) {
        logger.info(s"\tIndexed spectrum #$counter with id ${spectrum.id}, index size = $indexSize")
      } else {
        logger.debug(s"\tIndexed spectrum #$counter with id ${spectrum.id}, index size = $indexSize")
      }
    }
  }
}
