package edu.ucdavis.fiehnlab.mona.core.similarity.service

import com.typesafe.scalalogging.LazyLogging
import edu.ucdavis.fiehnlab.mona.backend.core.domain.SpectrumResult
import edu.ucdavis.fiehnlab.mona.backend.core.domain.util.DynamicIterable
import edu.ucdavis.fiehnlab.mona.backend.core.domain.dao.{MetaDataDAO, Spectrum}
import edu.ucdavis.fiehnlab.mona.backend.core.persistence.postgresql.repository.SpectrumResultRepository
import edu.ucdavis.fiehnlab.mona.backend.curation.util.CommonMetaData
import edu.ucdavis.fiehnlab.mona.core.similarity.types.IndexType.IndexType
import edu.ucdavis.fiehnlab.mona.core.similarity.types.{IndexType, SimpleSpectrum}
import edu.ucdavis.fiehnlab.mona.core.similarity.util.IndexUtils
import org.springframework.beans.factory.annotation.{Autowired, Value}
import org.springframework.boot.context.event.ApplicationReadyEvent
import org.springframework.context.ApplicationListener
import org.springframework.data.domain.{Page, Pageable}
import org.springframework.stereotype.Service

import scala.jdk.CollectionConverters._
import scala.collection.mutable.Buffer

/**
  * Created by sajjan on 1/3/17.
  */
@Service
class SimilarityStartupService extends ApplicationListener[ApplicationReadyEvent] with LazyLogging {

  @Autowired
  val spectrumResultRepository: SpectrumResultRepository = null

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


  private def addToIndex(spectrum: Spectrum, indexName: String, indexType: IndexType): Int = {
    val precursorMZ: Option[MetaDataDAO] = spectrum.getMetaData.asScala.find(_.getName == CommonMetaData.PRECURSOR_MASS)
    val tags: Buffer[String] = spectrum.getTags.asScala.map(_.getText)

    if (precursorMZ.isDefined) {
      try {
        val precursorString: String = precursorMZ.get.getValue.toString
        indexUtils.addToIndex(new SimpleSpectrum(spectrum.getId, spectrum.getSpectrum, precursorString.toDouble, tags.toArray), indexName, indexType)
      } catch {
        case _: Throwable => indexUtils.addToIndex(new SimpleSpectrum(spectrum.getId, spectrum.getSpectrum, tags.toArray), indexName, indexType)
      }
    } else {
      indexUtils.addToIndex(new SimpleSpectrum(spectrum.getId, spectrum.getSpectrum, tags.toArray), indexName, indexType)
    }
  }


  def populateIndices(): Unit = {
    logger.info("Populating indices...")

    val it = new DynamicIterable[SpectrumResult, String](null, 10) {
      override def fetchMoreData(query: String, pageable: Pageable): Page[SpectrumResult] = {
        spectrumResultRepository.findAll(pageable)
      }
    }.iterator

    var counter: Int = 0

    while (it.hasNext) {
      val spectrum: Spectrum = it.next().getSpectrum

      // Add precursor information if available
      val mainIndexSize: Int = addToIndex(spectrum, "default", IndexType.DEFAULT)
      val peakIndexSize: Int = addToIndex(spectrum, "default", IndexType.PEAK)

      counter += 1

      if (counter % 10000 == 0) {
        logger.info(s"\tIndexed spectrum #$counter with id ${spectrum.getId}, main index size = $mainIndexSize, peak index size = $peakIndexSize")
      } else {
        logger.debug(s"\tIndexed spectrum #$counter with id ${spectrum.getId}, main index size = $mainIndexSize, peak index size = $peakIndexSize")
      }
    }

    logger.info(s"\tFinished indexing $counter spectrum, index size = ${indexUtils.getIndexSize}")
  }
}
