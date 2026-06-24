package edu.ucdavis.fiehnlab.mona.backend.services.downloader.runner.service

import java.lang.Iterable
import com.typesafe.scalalogging.LazyLogging
import edu.ucdavis.fiehnlab.mona.backend.core.domain.Spectrum
import edu.ucdavis.fiehnlab.mona.backend.core.persistence.postgresql.service.SpectrumPersistenceService
import edu.ucdavis.fiehnlab.mona.backend.services.downloader.runner.writer._
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.context.annotation.Profile
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional

import javax.persistence.EntityManager

/**
  * Created by sajjan on 6/10/16.
  */
@Service
@Profile(Array("mona.persistence.downloader"))
class DownloadWriterService extends LazyLogging {

  @Autowired
  val spectrumPersistenceService: SpectrumPersistenceService = null

  @Autowired
  private val entityManager: EntityManager = null


  /**
    * Return the results of a given query as a paginated iterator
    *
    * @param query
    */
  // Page size for keyset based export paging
  private val exportPageSize: Int = 1000

  private def executeQuery(query: String): Iterable[Spectrum] = new Iterable[Spectrum] {

    /**
      * Walks the id ordering with a cursor (id < lastId) instead of offset paging, so there is no
      * per-page count query and no growing offset on large exports
      */
    override def iterator(): java.util.Iterator[Spectrum] = new java.util.Iterator[Spectrum] {
      private var buffer: java.util.List[Spectrum] = spectrumPersistenceService.findAllForExport(query, null, exportPageSize)
      private var index: Int = 0
      private var lastId: String = if (buffer.isEmpty) null else buffer.get(buffer.size - 1).getId

      override def hasNext: Boolean = {
        if (index < buffer.size) {
          true
        } else if (buffer.size < exportPageSize) {
          // The last page was partial, so there is nothing left to fetch
          false
        } else {
          // Fetch the next keyset page starting after the last id we returned
          buffer = spectrumPersistenceService.findAllForExport(query, lastId, exportPageSize)
          index = 0
          if (!buffer.isEmpty) {
            lastId = buffer.get(buffer.size - 1).getId
          }
          index < buffer.size
        }
      }

      override def next(): Spectrum = {
        val spectrum = buffer.get(index)
        index += 1
        spectrum
      }
    }
  }

  /**
    * Find the spectrum count for a given query
    *
    * @param query
    * @return
    */
  private def countQuery(query: String): Long = {
    if (query == null || query.isEmpty) {
      spectrumPersistenceService.count()
    } else {
      spectrumPersistenceService.count(query)
    }
  }

  /**
    *
    * @param query
    * @param label
    * @param downloaders
    * @return
    */
  @Transactional
  def exportQuery(query: String, label: String, downloaders: Array[SpectrumDownloader]): Long = {

    var count: Long = 0
    val total: Long = countQuery(query)

    logger.info(s"$label: Starting export of $total spectra")

    // Initialize each downloader
    downloaders.foreach(_.initializeExport())

    // Start querying the database
    val it = executeQuery(query).iterator

    while (it.hasNext) {
      val spectrum: Spectrum = it.next()

      // Write the current spectrum for each output format
      downloaders.foreach(_.write(spectrum))
      count += 1

      if (count % 1000 == 0) {
        logger.info(s"$label: Exported $count/$total")
      }
      entityManager.detach(spectrum)
    }



    // Close each downloader and compress if requested
    downloaders.foreach(_.closeExport())

    total
  }
}
