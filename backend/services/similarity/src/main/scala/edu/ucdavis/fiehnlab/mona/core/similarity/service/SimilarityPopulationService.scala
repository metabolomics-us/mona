package edu.ucdavis.fiehnlab.mona.core.similarity.service

import com.typesafe.scalalogging.LazyLogging
import edu.ucdavis.fiehnlab.mona.backend.core.domain.{Compound, MetaData, Spectrum}
import edu.ucdavis.fiehnlab.mona.backend.core.persistence.postgresql.repository.SpectrumRepository
import edu.ucdavis.fiehnlab.mona.backend.curation.util.CommonMetaData
import edu.ucdavis.fiehnlab.mona.core.similarity.types.IndexType.IndexType
import edu.ucdavis.fiehnlab.mona.core.similarity.types.{IndexType, SimpleSpectrum}
import edu.ucdavis.fiehnlab.mona.core.similarity.util.IndexUtils
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional

import javax.persistence.EntityManager
import scala.collection.mutable
import scala.jdk.CollectionConverters._
import scala.jdk.StreamConverters.StreamHasToScala


@Service
class SimilarityPopulationService extends LazyLogging {
  @Autowired
  val indexUtils: IndexUtils = null

  @Autowired
  val spectrumRepository: SpectrumRepository = null

  @Autowired
  private val entityManager: EntityManager = null

  private def addToIndex(spectrum: Spectrum, indexName: String, indexType: IndexType): Int = {
    val precursorMZ: Option[MetaData] = spectrum.getMetaData.asScala.find(_.getName == CommonMetaData.PRECURSOR_MASS)
    val tags: mutable.Buffer[String] = spectrum.getTags.asScala.map(_.getText)
    val biologicalCompound: Compound =
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

  @Transactional()
  def populateIndices(): Unit = {
    logger.info("Populating indices...")

    var counter: Int = 0
    spectrumRepository.streamAllBy().toScala(Iterator).foreach { spectrum =>
      // Add precursor information if available
      try {
        val mainIndexSize: Int = addToIndex(spectrum, "default", IndexType.DEFAULT)
        val peakIndexSize: Int = addToIndex(spectrum, "default", IndexType.PEAK)

        counter += 1

        if (counter % 10000 == 0) {
          logger.info(s"\tIndexed spectrum #$counter with id ${spectrum.getId}, main index size = $mainIndexSize, peak index size = $peakIndexSize")
        } else {
          logger.debug(s"\tIndexed spectrum #$counter with id ${spectrum.getId}, main index size = $mainIndexSize, peak index size = $peakIndexSize")
        }
      } catch {
        case nfe: NumberFormatException => logger.error(s"Invalid spectrum: ${spectrum}")
      }
      entityManager.detach(spectrum)
    }
    logger.info(s"\tFinished indexing $counter spectrum, index size = ${indexUtils.getIndexSize}")
  }
}

