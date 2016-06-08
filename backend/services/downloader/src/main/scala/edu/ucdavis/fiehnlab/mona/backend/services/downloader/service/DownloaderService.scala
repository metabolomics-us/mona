package edu.ucdavis.fiehnlab.mona.backend.services.downloader.service

import java.io.{FileNotFoundException, File}
import java.nio.file.{Paths, Files}

import com.typesafe.scalalogging.LazyLogging
import edu.ucdavis.fiehnlab.mona.backend.core.domain.Spectrum
import edu.ucdavis.fiehnlab.mona.backend.services.downloader.QueryExport
import edu.ucdavis.fiehnlab.mona.backend.services.downloader.repository.IQueryExportMongoRepository
import org.springframework.beans.factory.annotation.{Value, Autowired}
import org.springframework.stereotype.Service

/**
  * Created by sajjan on 5/25/16.
  */
@Service
class DownloaderService extends LazyLogging {

  @Autowired
  val queryExportMongoRepository: IQueryExportMongoRepository = null

  @Value("${mona.export.path:#{systemProperties['java.io.tmpdir']}}#{systemProperties['file.separator']}mona_exports")
  val dir: String = null

  @Value("${mona.export.querySize:100}")
  val querySize: Int = -1

  /**
    *
    * @param label
    * @return
    */
  def sanitizeLabel(label: String): String = {
    label.replaceAll(" ", "_").replaceAll("/", "-")
  }

  /**
    *
    * @param export
    */
  def download(export: QueryExport) = {
    // Create export directory if needed
    val directory: File = new File(dir)

    if (!directory.exists()) {
      if (!directory.mkdirs()) {
        throw new FileNotFoundException("was not able to create storage directory at: ${downloadPath}")
      }
    }

    // Generate export filenames
    val queryFilename = s"${sanitizeLabel(export.label)}-query.txt"
    val exportFilename = s"${sanitizeLabel(export.label)}.${export.format}"
    val compressedExportFilename = s"${sanitizeLabel(export.label)}-${export.format}.zip"

    // Export query string
    Files.write(Paths.get(dir, queryFilename), export.query.getBytes)

    // Export query results
    val bufferedWriter = Files.newBufferedWriter(Paths.get(dir, exportFilename))

    logger.info("START")
    logger.info(export.query)
    logger.info(querySize.toString)

    //    val restRepositoryReader: RestRepositoryReader = new RestRepositoryReader(export.query, querySize)

    var count: Int = 0
    var spectrum: Spectrum = null

    //    do {
    //      logger.info("ITERATION")
    //      spectrum = restRepositoryReader.read()
    //
    //      if (spectrum != null)
    //        count += 1
    //    }  while (spectrum != null)

    logger.info(count.toString)
  }
}
