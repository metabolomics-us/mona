package edu.ucdavis.fiehnlab.mona.core.similarity.service

import com.typesafe.scalalogging.LazyLogging
import edu.ucdavis.fiehnlab.mona.backend.core.domain.dao.{CompoundDAO, MetaDataDAO, Spectrum}
import edu.ucdavis.fiehnlab.mona.backend.core.domain.util.DynamicIterable
import edu.ucdavis.fiehnlab.mona.backend.core.persistence.postgresql.repository.SpectrumRepository
import edu.ucdavis.fiehnlab.mona.backend.curation.util.CommonMetaData
import edu.ucdavis.fiehnlab.mona.core.similarity.types.IndexType.IndexType
import edu.ucdavis.fiehnlab.mona.core.similarity.types.{IndexType, SimpleSpectrum}
import edu.ucdavis.fiehnlab.mona.core.similarity.util.IndexUtils
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.data.domain.{Page, Pageable}
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional

import scala.collection.mutable
import scala.jdk.CollectionConverters._


@Service
class SimilarityPopulationService extends LazyLogging{
  @Autowired
  val indexUtils: IndexUtils = null

  @Autowired
  val spectrumRepository: SpectrumRepository = null

  @Transactional(propagation =  org.springframework.transaction.annotation.Propagation.REQUIRES_NEW)
  private def addToIndex(spectrum: Spectrum, indexName: String, indexType: IndexType): Int = {
    val precursorMZ: Option[MetaDataDAO] = spectrum.getMetaData.asScala.find(_.getName == CommonMetaData.PRECURSOR_MASS)
    val tags: mutable.Buffer[String] = spectrum.getTags.asScala.map(_.getText)
    val biologicalCompound: CompoundDAO =
      if (spectrum.getCompound.asScala.exists(_.getKind == "biological")) {
        spectrum.getCompound.asScala.find(_.getKind == "biological").head
      } else if (spectrum.getCompound.asScala.nonEmpty) {
        spectrum.getCompound.asScala.head
      } else {
        null
      }
    val theoreticalAdducts: mutable.Buffer[Double] = biologicalCompound.getMetaData.asScala.filter(x => x.getCategory == "theoretical adduct").map(_.getValue.toDouble)

    if (precursorMZ.isDefined) {
      try {
        val precursorString: String = precursorMZ.get.getValue
        indexUtils.addToIndex(new SimpleSpectrum(spectrum.getId, spectrum.getSpectrum, precursorString.toDouble, tags.toArray, theoreticalAdducts.toArray), indexName, indexType)
      } catch {
        case _: Throwable => indexUtils.addToIndex(new SimpleSpectrum(spectrum.getId, spectrum.getSpectrum, tags.toArray, theoreticalAdducts.toArray), indexName, indexType)
      }
    } else {
      indexUtils.addToIndex(new SimpleSpectrum(spectrum.getId, spectrum.getSpectrum, tags.toArray, theoreticalAdducts.toArray), indexName, indexType)
    }
  }

  @Transactional(propagation =  org.springframework.transaction.annotation.Propagation.REQUIRES_NEW)
  def populateIndices(): Unit = {
    logger.info("Populating indices...")

    val it = new DynamicIterable[Spectrum, String](null, 200) {
      override def fetchMoreData(query: String, pageable: Pageable): Page[Spectrum] = {
        spectrumRepository.findAll(pageable)
      }
    }.iterator

    var counter: Int = 0

    while (it.hasNext) {
      val spectrum: Spectrum = it.next()

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

