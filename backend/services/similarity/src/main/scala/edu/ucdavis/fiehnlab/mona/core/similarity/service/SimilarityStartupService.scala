package edu.ucdavis.fiehnlab.mona.core.similarity.service

import com.typesafe.scalalogging.LazyLogging
import edu.ucdavis.fiehnlab.mona.backend.core.domain.util.DynamicIterable
import edu.ucdavis.fiehnlab.mona.backend.core.domain.{MetaData, Spectrum}
import edu.ucdavis.fiehnlab.mona.backend.core.persistence.mongo.repository.ISpectrumMongoRepositoryCustom
import edu.ucdavis.fiehnlab.mona.backend.curation.util.CommonMetaData
import edu.ucdavis.fiehnlab.mona.core.similarity.types.IndexType.IndexType
import edu.ucdavis.fiehnlab.mona.core.similarity.types.{IndexType, SimpleSpectrum}
import edu.ucdavis.fiehnlab.mona.core.similarity.util.IndexUtils
import org.springframework.beans.factory.annotation.{Autowired, Value}
import org.springframework.boot.context.event.ApplicationReadyEvent
import org.springframework.context.ApplicationListener
import org.springframework.data.domain.{Page, Pageable}
import org.springframework.stereotype.Service

/**
  * Created by sajjan on 1/3/17.
  */
@Service
class SimilarityStartupService extends ApplicationListener[ApplicationReadyEvent] with LazyLogging {

  @Autowired
  val spectrumMongoRepository: ISpectrumMongoRepositoryCustom = null

  @Autowired
  val indexUtils: IndexUtils = null

  @Value("${mona.similarity.autopopulate:true}")
  private val autoPopulate: Boolean = true

  override def onApplicationEvent(e: ApplicationReadyEvent): Unit = {
    if (autoPopulate) {
      logger.info("Starting auto-population of indices")
      populateIndices()
    }
  }


  private def addToIndex(spectrum: Spectrum, precursorMZ: Option[MetaData], indexName: String, indexType: IndexType): Int = {
    if (precursorMZ.isDefined) {
      try {
        val precursorString: String = precursorMZ.get.value.toString
        indexUtils.addToIndex(new SimpleSpectrum(spectrum.id, spectrum.spectrum, precursorString.toDouble), indexName, indexType)
      } catch {
        case _: Throwable => indexUtils.addToIndex(new SimpleSpectrum(spectrum.id, spectrum.spectrum), indexName, indexType)
      }
    } else {
      indexUtils.addToIndex(new SimpleSpectrum(spectrum.id, spectrum.spectrum), indexName, indexType)
    }
  }


  def populateIndices(): Unit = {
    logger.info("Populating indices...")

    val it = new DynamicIterable[Spectrum, String](null, 10) {
      override def fetchMoreData(query: String, pageable: Pageable): Page[Spectrum] = {
          spectrumMongoRepository.findAll(pageable)
      }
    }.iterator

    var counter: Int = 0

    while(it.hasNext) {
      val spectrum: Spectrum = it.next()
      val precursorMZ: Option[MetaData] = spectrum.metaData.find(_.name == CommonMetaData.PRECURSOR_MASS)

      // Add precursor information if available
      val mainIndexSize: Int = addToIndex(spectrum, precursorMZ, "default", IndexType.DEFAULT)
      val peakIndexSize: Int = addToIndex(spectrum, precursorMZ, "default", IndexType.PEAK)

      counter += 1

      if (counter % 10000 == 0) {
        logger.info(s"\tIndexed spectrum #$counter with id ${spectrum.id}, main index size = $mainIndexSize, peak index size = $peakIndexSize")
      } else {
        logger.debug(s"\tIndexed spectrum #$counter with id ${spectrum.id}, main index size = $mainIndexSize, peak index size = $peakIndexSize")
      }
    }

    logger.info(s"\tFinished indexing $counter spectrum, index size = ${indexUtils.getIndexSize}")
  }
}
