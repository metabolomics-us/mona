package edu.ucdavis.fiehnlab.mona.backend.services.downloader.service

import java.io._
import java.lang.Iterable
import java.nio.charset.StandardCharsets
import java.nio.file.{Path, Files, Paths}
import java.util.UUID
import java.util.zip.{ZipEntry, ZipOutputStream}

import com.fasterxml.jackson.databind.ObjectMapper
import com.typesafe.scalalogging.LazyLogging
import edu.ucdavis.fiehnlab.mona.backend.core.domain.Spectrum
import edu.ucdavis.fiehnlab.mona.backend.core.domain.io.json.MonaMapper
import edu.ucdavis.fiehnlab.mona.backend.core.domain.io.msp.MSPWriter
import edu.ucdavis.fiehnlab.mona.backend.core.domain.util.DynamicIterable
import edu.ucdavis.fiehnlab.mona.backend.core.persistence.mongo.repository.ISpectrumMongoRepositoryCustom
import edu.ucdavis.fiehnlab.mona.backend.services.downloader.QueryExport
import edu.ucdavis.fiehnlab.mona.backend.services.downloader.repository.QueryExportMongoRepository
import org.springframework.beans.factory.annotation.{Autowired, Value}
import org.springframework.data.domain.{Page, Pageable}
import org.springframework.stereotype.Service

import scala.collection.JavaConverters._

/**
  * Created by sajjan on 5/25/16.
  */
@Service
class DownloaderService extends LazyLogging {

  @Autowired
  val mongoRepository: ISpectrumMongoRepositoryCustom = null

  @Autowired
  val queryExportMongoRepository: QueryExportMongoRepository = null

  val objectMapper: ObjectMapper = MonaMapper.create

  @Value("${mona.export.path:#{systemProperties['java.io.tmpdir']}}#{systemProperties['file.separator']}mona_exports")
  val dir: String = null

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
    * @param query
    */
  def executeQuery(query: String): Iterable[Spectrum] = {
    if (query == null || query.isEmpty) {
      mongoRepository.findAll
    } else {
      new DynamicIterable[Spectrum, String](query, 10) {

        /**
          * Loads more data from the server for the given query
          */
        override def fetchMoreData(query: String, pageable: Pageable): Page[Spectrum] = {
          mongoRepository.rsqlQuery(query, pageable)
        }
      }
    }
  }

  /**
    *
    * @param bufferedWriter
    * @param query
    * @return
    */
  def downloadAsJSON(bufferedWriter: BufferedWriter, query: String): Int = {
    bufferedWriter.write("[")

    val count: Int = executeQuery(query).asScala.foldLeft(0) { (sum, spectrum: Spectrum) =>
      if (sum > 0) {
        bufferedWriter.write(",")
      }

      bufferedWriter.write(objectMapper.writeValueAsString(spectrum))
      sum + 1
    }

    bufferedWriter.write("]")

    count
  }

  /**
    *
    * @param bufferedWriter
    * @param query
    * @return
    */
  def downloadAsMSP(bufferedWriter: BufferedWriter, query: String): Int = {
    val mspWriter: MSPWriter = new MSPWriter

    val count: Int = executeQuery(query).asScala.foldLeft(0) { (sum, spectrum: Spectrum) =>
      if (sum > 0) {
        bufferedWriter.write("\n")
      }

      mspWriter.write(spectrum, bufferedWriter)
      sum + 1
    }

    count
  }

  /**
    *
    * @param export
    */
  def download(export: QueryExport): QueryExport = download(export, compressExport = true)

  def download(export: QueryExport, compressExport: Boolean): QueryExport = {
    // Create export directory if needed
    val directory: File = new File(dir)

    if (!directory.exists()) {
      if (!directory.mkdirs()) {
        throw new FileNotFoundException("was not able to create storage directory at: ${downloadPath}")
      }
    }

    // Generate label if one does not exist
    val label: String =
      if (export.label == null || export.label.isEmpty)
        UUID.randomUUID.toString
      else
        export.label


    logger.info(s"Starting download for label $label")
    logger.info(s"Query: ${export.query}")

    // Generate export filenames
    val basename: String = buildExportBasename(label, export.emailAddress)

    val queryFilename: String = s"$basename-query.txt"
    val exportFilename: String = s"$basename.${export.format}"
    val compressedExportFilename: String = s"$basename-${export.format}.zip"


    // Export query string
    logger.info(s"Exporting query file $queryFilename")
    Files.write(Paths.get(dir, queryFilename), export.query.getBytes(StandardCharsets.UTF_8))


    // Export query results
    logger.info(s"Exporting spectra to file $exportFilename")

    val exportFile: Path = Paths.get(dir, exportFilename)
    val bufferedWriter = Files.newBufferedWriter(exportFile)

    val count: Int = export.format match {
      case "msp" => downloadAsMSP(bufferedWriter, export.query)
      case "json" | _ => downloadAsJSON(bufferedWriter, export.query)
    }

    bufferedWriter.close()

    logger.info(s"Finished exporting $count spectra")


    // Compress results
    if (compressExport) {
      logger.info("Compressing ${exportFilename} -> ${compressedExportFilename}")

      val compressedFile: Path = Paths.get(dir, compressedExportFilename)
      val compressedTemporaryFile: Path = Paths.get(dir, compressedExportFilename +".tmp")

      val zipFile: ZipOutputStream = new ZipOutputStream(new FileOutputStream(compressedTemporaryFile.toAbsolutePath.toString))

      zipFile.putNextEntry(new ZipEntry(exportFilename))

      val inputStream: BufferedInputStream = new BufferedInputStream(new FileInputStream(Paths.get(dir, exportFilename).toAbsolutePath.toString))
      val buffer: Array[Byte] = new Array[Byte](1024)
      var length: Int = inputStream.read(buffer, 0, 1024)


      while (length != -1) {
        zipFile.write(buffer, 0, length)
        length = inputStream.read(buffer, 0, 1024)
      }

      inputStream.close()
      zipFile.closeEntry()
      zipFile.close()

      // Delete exported file and move temporary export file to stored location
      Files.deleteIfExists(exportFile)
      Files.deleteIfExists(compressedFile)
      Files.move(compressedTemporaryFile, compressedFile)


      // Update export object with filesize, query count and filenames
      export.copy(
        label = label,
        count = count,
        size = Files.size(compressedFile),

        queryFile = queryFilename,
        exportFile = compressedExportFilename
      )
    } else {
      // Update export object with filesize, query count and filenames
      export.copy(
        label = label,
        count = count,
        size = Files.size(exportFile),

        queryFile = queryFilename,
        exportFile = exportFilename
      )
    }
  }
}
