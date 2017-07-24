package edu.ucdavis.fiehnlab.mona.backend.services.downloader.runner.writer

import java.io.BufferedWriter
import java.lang.Iterable
import java.nio.file.{Files, Path}

import com.typesafe.scalalogging.LazyLogging
import edu.ucdavis.fiehnlab.mona.backend.core.domain.Spectrum
import edu.ucdavis.fiehnlab.mona.backend.core.domain.util.DynamicIterable
import edu.ucdavis.fiehnlab.mona.backend.core.persistence.mongo.repository.ISpectrumMongoRepositoryCustom
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.data.domain.{Page, Pageable}
import org.springframework.stereotype.Service

/**
  * Created by sajjan on 9/13/16.
  */
@Service
abstract class AbstractDownloader extends LazyLogging {

  @Autowired
  val mongoRepository: ISpectrumMongoRepositoryCustom = null


  /**
    * Return the results of a given query as a paginated iterator
    * @param query
    */
  def executeQuery(query: String): Iterable[Spectrum] = {
    new DynamicIterable[Spectrum, String](query, 25) {

      /**
        * Loads more data from the server for the given query
        */
      override def fetchMoreData(query: String, pageable: Pageable): Page[Spectrum] = {
        if (query == null || query.isEmpty) {
          mongoRepository.findAll(pageable)
        } else {
          mongoRepository.rsqlQuery(query, pageable)
        }
      }
    }
  }

  /**
    * Find the spectrum count for a given query
    * @param query
    * @return
    */
  def countQuery(query: String): Long = {
    if (query == null || query.isEmpty) {
      mongoRepository.count()
    } else {
      mongoRepository.rsqlQueryCount(query)
    }
  }


  /**
    * File format prefix
    * @return
    */
  def getFilePrefix: String

  /**
    * File format suffix
    * @return
    */
  def getFileSuffix: String

  /**
    * File format separator
    * @return
    */
  def getFileSeparator: String

  /**
    *
    */
  def writeSpectrum(spectrum: Spectrum, bufferedWriter: BufferedWriter)


  /**
    * Export file
    */
  def write(query: String, exportFile: Path): Long = {
    val bufferedWriter = Files.newBufferedWriter(exportFile)
    bufferedWriter.write(getFilePrefix)

    var count: Long = 0
    val total: Long = countQuery(query)

    logger.info(s"${exportFile.getFileName}: Starting export of $total spectra")

    val it = executeQuery(query).iterator

    while(it.hasNext) {
      val spectrum = it.next()

      if (count > 0) {
        bufferedWriter.write(getFileSeparator)
      }

      writeSpectrum(spectrum, bufferedWriter)
      count += 1

      if (count % 1000 == 0) {
        logger.info(s"${exportFile.getFileName}: Exported $count/$total")
      }
    }

    bufferedWriter.write(getFileSuffix)
    bufferedWriter.close()

    count
  }
}
