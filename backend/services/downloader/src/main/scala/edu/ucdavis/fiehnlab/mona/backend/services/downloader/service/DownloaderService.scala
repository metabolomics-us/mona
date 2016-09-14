package edu.ucdavis.fiehnlab.mona.backend.services.downloader.service

import java.io._
import java.nio.file.{Files, Path, Paths}
import java.util.Date

import com.fasterxml.jackson.databind.ObjectMapper
import com.typesafe.scalalogging.LazyLogging
import edu.ucdavis.fiehnlab.mona.backend.core.domain.io.json.MonaMapper
import edu.ucdavis.fiehnlab.mona.backend.services.downloader.types.QueryExport
import org.springframework.beans.factory.annotation.{Autowired, Value}
import org.springframework.stereotype.Service

/**
  * Created by sajjan on 5/25/16.
  */
@Service
class DownloaderService extends LazyLogging {

  @Autowired
  val downloadWriterService: DownloadWriterService = null

  @Value("${mona.export.path:#{systemProperties['java.io.tmpdir']}}#{systemProperties['file.separator']}mona_exports")
  val exportDir: String = null

  val objectMapper: ObjectMapper = MonaMapper.create

  /**
    *
    * @param label
    * @param emailAddress
    * @return
    */
  def buildExportBasename(label: String, emailAddress: String): String = {
    val sanitizedLabel: String = label.replaceAll(" ", "_").replaceAll("/", "-")

    if (emailAddress == null || emailAddress.isEmpty) {
      sanitizedLabel
    } else {
      emailAddress.split("@") + sanitizedLabel
    }
  }

  /**
    *
    * @param export
    */
  def download(export: QueryExport): QueryExport = download(export, compressExport = true)

  def download(export: QueryExport, compressExport: Boolean): QueryExport = {
    // Create export directory if needed
    val directory: File = new File(exportDir)

    if (!directory.exists()) {
      if (!directory.mkdirs()) {
        throw new FileNotFoundException(s"was not able to create storage directory at: $exportDir")
      }
    }

    // Use the export ID as the label if one does not exist
    val label: String =
      if(export.label == null || export.label.isEmpty)
        export.id
      else
        export.label


    logger.info(s"Starting download for label $label")
    logger.info(s"Query: ${export.query}")

    // Generate export filenames
    val basename: String = buildExportBasename(label, export.emailAddress)

    val queryFilename: String = s"MoNA-export-$basename-query.txt"
    val exportFilename: String = s"MoNA-export-$basename.${export.format}"
    val compressedExportFilename: String = s"MoNA-export-$basename-${export.format}.zip"


    // Export query string
    val queryFile: Path = Paths.get(exportDir, queryFilename)

    downloadWriterService.writeQueryFile(queryFile, export.query)


    // Export query results
    val exportFile: Path = Paths.get(exportDir, exportFilename)
    val compressedFile: Path = Paths.get(exportDir, compressedExportFilename)

    val count: Long = downloadWriterService.writeExportFile(exportFile, compressedFile, export.query, export.format, compressExport)

    if (compressExport) {
      // Update export object with filesize, query count and filenames
      export.copy(
        label = label,
        count = count,
        date = new Date,
        size = Files.size(compressedFile),

        queryFile = queryFilename,
        exportFile = compressedExportFilename
      )
    } else {
      // Update export object with filesize, query count and filenames
      export.copy(
        label = label,
        count = count,
        date = new Date,
        size = Files.size(exportFile),

        queryFile = queryFilename,
        exportFile = exportFilename
      )
    }
  }
}
